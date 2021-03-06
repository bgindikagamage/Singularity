package com.hubspot.singularity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Objects;
import java.util.Optional;

@Schema(description = "Describes a completed deploy")
public class SingularityDeployHistory
  implements Comparable<SingularityDeployHistory>, SingularityHistoryItem {
  private final Optional<SingularityDeployResult> deployResult;
  private final SingularityDeployMarker deployMarker;
  private final Optional<SingularityDeploy> deploy;
  private final Optional<SingularityDeployStatistics> deployStatistics;

  @JsonCreator
  public SingularityDeployHistory(
    @JsonProperty("deployResult") Optional<SingularityDeployResult> deployResult,
    @JsonProperty("deployMarker") SingularityDeployMarker deployMarker,
    @JsonProperty("deploy") Optional<SingularityDeploy> deploy,
    @JsonProperty(
      "deployStatistics"
    ) Optional<SingularityDeployStatistics> deployStatistics
  ) {
    this.deployResult = deployResult;
    this.deployMarker = deployMarker;
    this.deploy = deploy;
    this.deployStatistics = deployStatistics;
  }

  @Override
  public int compareTo(SingularityDeployHistory o) {
    return o.getDeployMarker().compareTo(getDeployMarker());
  }

  @Schema(description = "The result of the deploy", nullable = true)
  public Optional<SingularityDeployResult> getDeployResult() {
    return deployResult;
  }

  @Schema(description = "Uniquely identifies the deploy")
  public SingularityDeployMarker getDeployMarker() {
    return deployMarker;
  }

  @Schema(description = "Full deploy data", nullable = true)
  public Optional<SingularityDeploy> getDeploy() {
    return deploy;
  }

  @Override
  @JsonIgnore
  public long getCreateTimestampForCalculatingHistoryAge() {
    return deployMarker.getTimestamp();
  }

  @Schema(description = "Task statistics related to this deploy")
  public Optional<SingularityDeployStatistics> getDeployStatistics() {
    return deployStatistics;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SingularityDeployHistory that = (SingularityDeployHistory) o;
    return (
      Objects.equals(deployResult, that.deployResult) &&
      Objects.equals(deployMarker, that.deployMarker) &&
      Objects.equals(deploy, that.deploy) &&
      Objects.equals(deployStatistics, that.deployStatistics)
    );
  }

  @Override
  public int hashCode() {
    return Objects.hash(deployResult, deployMarker, deploy, deployStatistics);
  }

  @Override
  public String toString() {
    return (
      "SingularityDeployHistory{" +
      "deployResult=" +
      deployResult +
      ", deployMarker=" +
      deployMarker +
      ", deploy=" +
      deploy +
      ", deployStatistics=" +
      deployStatistics +
      '}'
    );
  }
}
