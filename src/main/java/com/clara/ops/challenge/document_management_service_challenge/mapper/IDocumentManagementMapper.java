package com.clara.ops.challenge.document_management_service_challenge.mapper;

import com.clara.ops.challenge.document_management_service_challenge.controller.dto.request.DocumentSearchFilters;
import com.clara.ops.challenge.document_management_service_challenge.controller.dto.response.Document;
import com.clara.ops.challenge.document_management_service_challenge.controller.dto.response.DocumentDownloadUrl;
import com.clara.ops.challenge.document_management_service_challenge.controller.dto.response.Metadata;
import com.clara.ops.challenge.document_management_service_challenge.controller.dto.response.PaginatedDocumentSearch;
import com.clara.ops.challenge.document_management_service_challenge.service.dto.*;
import java.util.List;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import org.springframework.web.multipart.MultipartFile;

@Mapper(componentModel = "spring")
public interface IDocumentManagementMapper {

  IDocumentManagementMapper INSTANCE = Mappers.getMapper(IDocumentManagementMapper.class);

  @Mapping(target = "user", source = "user")
  @Mapping(target = "name", source = "name")
  @Mapping(target = "tags", source = "tags")
  @Mapping(target = "file", source = "file")
  UploadDocumentDTO mapToUploadDocumentDTO(
      String user, String name, List<String> tags, MultipartFile file);

  @Mapping(target = "id", source = "id")
  @Mapping(target = "user", source = "user")
  @Mapping(target = "name", source = "name")
  @Mapping(target = "tags", source = "tags")
  @Mapping(target = "size", source = "size")
  @Mapping(target = "type", source = "type")
  @Mapping(target = "createdAt", source = "createdAt")
  Document mapToDocument(DocumentDTO documentDTO);

  @Mapping(target = "user", source = "filter.user")
  @Mapping(target = "name", source = "filter.name")
  @Mapping(target = "tags", source = "filter.tags")
  @Mapping(target = "page", source = "page")
  @Mapping(target = "size", source = "size")
  @Mapping(target = "sort", source = "sort")
  DocumentSearchDTO mapToDocumentSearchDTO(
      DocumentSearchFilters filter, Integer page, Integer size, String sort);

  PaginatedDocumentSearch mapToPaginatedDocumentSearch(
      PaginatedDocumentSearchDTO paginatedDocumentSearchDTO);

  @Mapping(target = "currentPage", source = "currentPage")
  @Mapping(target = "itemsPerPage", source = "itemsPerPage")
  @Mapping(target = "currentItems", source = "currentItems")
  @Mapping(target = "totalPages", source = "totalPages")
  @Mapping(target = "totalItems", source = "totalItems")
  Metadata mapToMetadata(MetadataDTO metadataDTO);

  @Mapping(target = "url", source = "url")
  DocumentDownloadUrl mapDocumentDownloadUrl(DocumentDownloadUrlDTO documentDownloadUrlDTO);
}
