package eu.dissco.core.datacitepublisher.domain.datacite;

import static eu.dissco.core.datacitepublisher.domain.datacite.DataCiteConstants.DC_EVENT;
import static eu.dissco.core.datacitepublisher.domain.datacite.DataCiteConstants.PUBLISHER;
import static eu.dissco.core.datacitepublisher.domain.datacite.DataCiteConstants.SCHEMA_VERSION;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@JsonInclude(Include.NON_NULL)
@NoArgsConstructor
public class DcAttributes {

  private String suffix;
  private String doi;
  private List<DcCreator> creators; // IssuedForAgent
  private List<DcTitle> titles; // ReferentName
  private Integer publicationYear; // From issueDate
  private List<DcSubject> subjects; // topic origin, topic domain, topic discipline, topic category (last one to do)
  private List<DcContributor> contributors; // SpecimenHost
  private List<DcDate> dates; // IssueDate
  private List<DcAlternateIdentifier> alternateIdentifiers; // primarySpecimenObjectId
  private DcType types; // "Digital Specimen" or "Media Object". Resource Type General = Other (TBD)
  private List<DcRelatedIdentifiers> relatedIdentifiers; // tombstone pids; primary specimenObjectId; primaryMediaId
  private List<DcDescription> descriptions; // Specimen: Host + materialSampleType, Media: host + linked object type
  private String url; // human readable landing page
  private DcPublisher publisher = PUBLISHER;
  private String schemaVersion = SCHEMA_VERSION;
  private String event = DC_EVENT;

  public DcAttributes withSuffix(String s){
    this.suffix = s;
    return this;
  }

  public DcAttributes withDoi(String s){
    this.doi = s;
    return this;
  }

  public DcAttributes withCreators(List<DcCreator> c){
    this.creators = c;
    return this;
  }

  public DcAttributes withTitles(List<DcTitle> t){
    this.titles = t;
    return this;
  }

  public DcAttributes withPublicationYear(Integer y){
    this.publicationYear = y;
    return this;
  }

  public DcAttributes withSubjects(List<DcSubject> s){
    this.subjects = s;
    return this;
  }

  public DcAttributes withContributors(List<DcContributor> c){
    this.contributors = c;
    return this;
  }

  public DcAttributes withDates(List<DcDate> d){
    this.dates = d;
    return this;
  }

  public DcAttributes withAlternateIdentifiers(List<DcAlternateIdentifier> a){
    this.alternateIdentifiers = a;
    return this;
  }

  public DcAttributes withType(DcType t){
    this.types = t;
    return this;
  }

  public DcAttributes withDescription(List<DcDescription> d){
    this.descriptions = d;
    return this;
  }

  public DcAttributes withRelatedIdentifiers(List<DcRelatedIdentifiers> r){
    this.relatedIdentifiers = r;
    return this;
  }

  public DcAttributes withUrl(String s){
    this.url = s;
    return this;
  }
}
