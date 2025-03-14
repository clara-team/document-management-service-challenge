package com.clara.ops.challenge.document_management_service_challenge.mapper;

import com.clara.ops.challenge.document_management_service_challenge.entity.Document;
import com.clara.ops.challenge.document_management_service_challenge.entity.Tag;
import com.clara.ops.challenge.document_management_service_challenge.service.dto.DocumentDTO;
import com.clara.ops.challenge.document_management_service_challenge.service.dto.MetadataDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IDocumentEntityMapper {

  @Mapping(target = "id", source = "id")
  @Mapping(target = "user", source = "user.name")
  @Mapping(target = "name", source = "name")
  @Mapping(target = "size", source = "fileSize")
  @Mapping(target = "type", source = "fileType")
  @Mapping(target = "createdAt", source = "createdAt")
  @Mapping(target = "pathFile", ignore = true)
  @Mapping(target = "tags", source = "tags")
  DocumentDTO mapToDocumentDTO(Document Document);

  @Mapping(target = "currentPage", source = "currentPage")
  @Mapping(target = "itemsPerPage", source = "itemsPerPage")
  @Mapping(target = "currentItems", source = "currentItems")
  @Mapping(target = "totalPages", source = "totalPages")
  @Mapping(target = "totalItems", source = "totalItems")
  MetadataDTO mapToMetadataDTO(
      Integer currentPage,
      Integer itemsPerPage,
      Integer currentItems,
      Integer totalPages,
      Integer totalItems);

  default String mapChildSourceToString(Tag tag) {
    return tag.getName();
  }
}
