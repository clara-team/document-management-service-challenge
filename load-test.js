import http from "k6/http";
import { check, group, sleep } from "k6";
import { Trend, Counter, Rate } from "k6/metrics";
import { htmlReport } from "https://raw.githubusercontent.com/benc-uk/k6-reporter/main/dist/bundle.js";

const uploadDuration = new Trend("upload_duration_ms", true);
const uploadErrors = new Counter("upload_errors");
const uploadSuccessRate = new Rate("upload_success_rate");

// ── Test configuration ────────────────────────────────────────────────────────
export const options = {
  scenarios: {
    concurrent_uploads: {
      executor: "per-vu-iterations",
      vus: 10,
      iterations: 1,
    },
  },
  thresholds: {
    upload_success_rate: ["rate>0.95"],
  },
};

const BASE_URL = "http://localhost:8080/document-management";

const FILE_PATH = "file_path"; // Replace with the actual path to your PDF file
const FILE_CONTENTS = open(FILE_PATH, "b");

const USERS = [
  "alice",
  "bob",
  "carlos",
  "diana",
  "john",
  "elena",
  "franco",
  "gabriela",
  "hector",
  "isabel",
];
const TAGS = [
  ["legal", "2024"],
  ["finanzas", "q1"],
  ["rrhh", "politica"],
  ["tecnologia", "propuesta"],
  ["legal", "contrato"],
  ["marketing", "campana"],
  ["operaciones", "logistica"],
  ["finanzas", "auditoria"],
  ["rrhh", "onboarding"],
  ["tecnologia", "seguridad"],
];

// ── Multipart builder ─────────────────────────────────────────────────────────
// k6 does not guarantee field order when using plain objects — files may be sent
// before text fields. We build the body manually to ensure metadata comes first.
function strToBytes(str) {
  const bytes = new Uint8Array(str.length);
  for (let i = 0; i < str.length; i++) {
    bytes[i] = str.charCodeAt(i) & 0xff;
  }
  return bytes;
}

function buildMultipart(user, name, tags, fileContents, fileName) {
  const boundary = "k6Boundary" + __VU + __ITER;
  const CRLF = "\r\n";

  function textPart(fieldName, value) {
    return strToBytes(
      `--${boundary}${CRLF}` +
        `Content-Disposition: form-data; name="${fieldName}"${CRLF}` +
        `${CRLF}${value}${CRLF}`,
    );
  }

  const headerParts = [
    textPart("user", user),
    textPart("name", name),
    ...tags.map((t) => textPart("tags", t)),
    strToBytes(
      `--${boundary}${CRLF}` +
        `Content-Disposition: form-data; name="file"; filename="${fileName}"${CRLF}` +
        `Content-Type: application/pdf${CRLF}${CRLF}`,
    ),
  ];
  const footer = strToBytes(`${CRLF}--${boundary}--${CRLF}`);
  const fileBytes = new Uint8Array(fileContents);

  const totalSize =
    headerParts.reduce((s, p) => s + p.byteLength, 0) +
    fileBytes.byteLength +
    footer.byteLength;

  const result = new Uint8Array(totalSize);
  let offset = 0;
  for (const part of headerParts) {
    result.set(part, offset);
    offset += part.byteLength;
  }
  result.set(fileBytes, offset);
  offset += fileBytes.byteLength;
  result.set(footer, offset);

  return {
    body: result.buffer,
    contentType: `multipart/form-data; boundary=${boundary}`,
  };
}

// ── Main scenario ─────────────────────────────────────────────────────────────
export default function () {
  const vuIndex = __VU % USERS.length;
  const user = USERS[vuIndex];
  const tags = TAGS[vuIndex];
  const fileName = `doc-vu${__VU}-iter${__ITER}.pdf`;

  const { body, contentType } = buildMultipart(
    user,
    fileName,
    tags,
    FILE_CONTENTS,
    fileName,
  );

  group(`VU${__VU} | user=${user} | file=${fileName}`, () => {
    const start = Date.now();

    const res = http.post(`${BASE_URL}/upload`, body, {
      headers: { "Content-Type": contentType },
      timeout: "3600s",
      tags: { name: "upload", user: user, vu: String(__VU) },
    });

    const duration = Date.now() - start;

    uploadDuration.add(duration, { user: user, vu: String(__VU) });

    const success = check(res, {
      "status 201": (r) => r.status === 201,
      duration: duration,
      "response time < 10min": () => duration < 600_000,
      "no error in body": (r) => !r.body || !r.body.includes("error"),
    });

    uploadSuccessRate.add(success, { user: user, vu: String(__VU) });

    if (!success) {
      uploadErrors.add(1, { user: user, vu: String(__VU) });
      console.error(
        `[VU ${__VU}][${user}] FAILED — status=${res.status} duration=${duration}ms body=${res.body}`,
      );
    } else {
      console.log(
        `[VU ${__VU}][${user}] OK — ${fileName} uploaded in ${duration}ms`,
      );
    }
  });

  sleep(1);
}

// ── Summary ───────────────────────────────────────────────────────────────────
export function handleSummary(data) {
  const successRate = (
    data.metrics.upload_success_rate.values.rate * 100
  ).toFixed(1);
  const p95 = data.metrics.upload_duration_ms?.values["p(95)"];
  const p50 = data.metrics.upload_duration_ms?.values["p(50)"];
  const total = data.metrics.upload_errors?.values.count ?? 0;

  console.log("\n╔══════════════════════════════════════╗");
  console.log("║         LOAD TEST SUMMARY            ║");
  console.log("╠══════════════════════════════════════╣");
  console.log(`║  Success rate : ${successRate}%`);
  console.log(`║  p50 duration : ${p50?.toFixed(0)}ms`);
  console.log(`║  p95 duration : ${p95?.toFixed(0)}ms`);
  console.log(`║  Total errors : ${total}`);
  console.log("╚══════════════════════════════════════╝\n");

  return {
    "load-test-results.json": JSON.stringify(data, null, 2),
    "load-test-report.html": htmlReport(data),
  };
}
