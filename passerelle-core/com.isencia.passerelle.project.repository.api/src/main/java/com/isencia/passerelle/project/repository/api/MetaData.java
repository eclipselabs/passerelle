package com.isencia.passerelle.project.repository.api;

public class MetaData implements Comparable<MetaData> {

  public MetaData(String code, String path) {
    super();
    this.code = code;
    this.name = code;
    this.path = path;
  }

  public MetaData(String type, Long id, String description, String code, String comment, String revision, String path) {
    this(type, id, description, code, comment, revision);
    this.path = path;

  }

  public MetaData(String type, Long id, String description, String code, String comment, String revision) {
    super();
    this.id = id;
    this.description = description;
    this.name = code;
    this.code = code;
    this.comment = comment;
    this.revision = revision;
    this.type = type;
  }

  private String type;

  private Long id;

  private String description;

  private String name;

  private String code;

  private String comment;

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  private String path;

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public String getRevision() {
    return revision;
  }

  public void setRevision(String revision) {
    this.revision = revision;
  }

  private String revision;

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int compareTo(MetaData arg0) {
    if (this.code != null)
      return this.code.compareTo(arg0.getCode());
    return this.name.compareTo(arg0.getName());
  }
}
