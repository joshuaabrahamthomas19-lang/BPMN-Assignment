package org.example.studio;

import io.camunda.client.CamundaClient;
import io.camunda.client.api.worker.JobWorker;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Component
@ConditionalOnProperty(name="studio.workers.enabled",havingValue="true",matchIfMissing=true)
public class WorkerRegistry {
    private final DemoApi api;
    private CamundaClient client;
    private final List<JobWorker> workers=new ArrayList<>();
    public WorkerRegistry(DemoApi api) { this.api=api; }
    @PostConstruct
    public void start() {
        client=CamundaClient.newClientBuilder().build();
        for(String type:LabLogic.TYPES) workers.add(client.newWorker().jobType(type).handler((jobClient,job)->{
                try {
                    if(api.failing("worker")) throw new IllegalStateException("Injected worker outage");
                    var output=LabLogic.run(type,job.getVariablesAsMap());
                    jobClient.newCompleteCommand(job.getKey()).variables(output).send().join();
                } catch(Exception e) {
                    jobClient.newFailCommand(job.getKey()).retries(Math.max(0,job.getRetries()-1))
                        .retryBackoff(Duration.ofSeconds(5)).errorMessage(e.getMessage()==null?"Worker failed":e.getMessage()).send().join();
                }
            }).name("workflow-studio").timeout(Duration.ofSeconds(30)).open());
    }
    @PreDestroy
    public void close() { workers.forEach(JobWorker::close);if(client!=null)client.close(); }
}
