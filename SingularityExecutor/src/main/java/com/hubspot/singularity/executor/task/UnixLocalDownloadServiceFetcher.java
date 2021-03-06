package com.hubspot.singularity.executor.task;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.hubspot.deploy.S3Artifact;
import com.hubspot.mesos.JavaUtils;
import com.hubspot.singularity.executor.config.SingularityExecutorConfiguration;
import com.hubspot.singularity.s3.base.ArtifactDownloadRequest;
import com.hubspot.singularity.s3.base.config.SingularityS3Configuration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.BytesContentProvider;
import org.eclipse.jetty.http.HttpMethod;

public class UnixLocalDownloadServiceFetcher implements LocalDownloadServiceFetcher {
  private final ObjectMapper objectMapper;
  private final HttpClient httpClient;
  private final SingularityExecutorConfiguration executorConfiguration;
  private final SingularityS3Configuration s3Configuration;
  private final String localDownloadPath;

  public UnixLocalDownloadServiceFetcher(
    HttpClient httpClient,
    ObjectMapper objectMapper,
    SingularityExecutorConfiguration executorConfiguration,
    SingularityS3Configuration s3Configuration
  ) {
    this.httpClient = httpClient;
    this.objectMapper = objectMapper;
    this.executorConfiguration = executorConfiguration;
    this.s3Configuration = s3Configuration;
    this.localDownloadPath =
      String.format("http://localhost%s", s3Configuration.getLocalDownloadPath());
  }

  @Override
  public void start() {
    try {
      httpClient.start();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void stop() {
    try {
      httpClient.stop();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String getDownloadPath() {
    return localDownloadPath;
  }

  @Override
  public void downloadFiles(
    List<? extends S3Artifact> s3Artifacts,
    SingularityExecutorTask task
  )
    throws InterruptedException {
    final List<CompletableFutureHolder> futures = Lists.newArrayListWithCapacity(
      s3Artifacts.size()
    );

    for (S3Artifact s3Artifact : s3Artifacts) {
      String destination = task
        .getArtifactPath(s3Artifact, task.getTaskDefinition().getTaskDirectoryPath())
        .toString();
      ArtifactDownloadRequest artifactDownloadRequest = new ArtifactDownloadRequest(
        destination,
        s3Artifact,
        Optional.of(executorConfiguration.getLocalDownloadServiceTimeoutMillis())
      );

      task
        .getLog()
        .debug(
          "Requesting {} from {}",
          artifactDownloadRequest,
          s3Configuration.getLocalDownloadSocket().get()
        );

      try {
        CompletableFuture<ContentResponse> future = new CompletableFuture<>();
        httpClient
          .newRequest(localDownloadPath)
          .method(HttpMethod.POST)
          .content(
            new BytesContentProvider(
              objectMapper.writeValueAsBytes(artifactDownloadRequest)
            ),
            "application/json"
          )
          .send(new CompletableFutureResponseListener(future));
        futures.add(
          new CompletableFutureHolder(future, System.currentTimeMillis(), s3Artifact)
        );
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }
    }

    for (CompletableFutureHolder future : futures) {
      ContentResponse response = future.getResponse();

      task
        .getLog()
        .debug(
          "Future for {} got status code {} after {}",
          future.s3Artifact.getName(),
          response.getStatus(),
          JavaUtils.duration(future.start)
        );

      if (response.getStatus() != 200) {
        throw new IllegalStateException("Got status code:" + response.getStatus());
      }
    }
  }

  private static class CompletableFutureHolder {
    private final CompletableFuture<ContentResponse> future;
    private final long start;
    private final S3Artifact s3Artifact;

    public CompletableFutureHolder(
      CompletableFuture<ContentResponse> future,
      long start,
      S3Artifact s3Artifact
    ) {
      this.future = future;
      this.start = start;
      this.s3Artifact = s3Artifact;
    }

    public ContentResponse getResponse() throws InterruptedException {
      try {
        return future.get();
      } catch (ExecutionException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
