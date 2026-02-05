package com.acme.web;

import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/flowable")
public class FlowableTestController {

  private final RepositoryService repositoryService;
  private final RuntimeService runtimeService;

  public FlowableTestController(RepositoryService repositoryService, RuntimeService runtimeService) {
    this.repositoryService = repositoryService;
    this.runtimeService = runtimeService;
  }

  @GetMapping("/deployments")
  public ResponseEntity<List<Map<String, Object>>> listDeployments() {
    List<Deployment> list = repositoryService.createDeploymentQuery().list();
    List<Map<String, Object>> result = list.stream()
        .map(d -> {
          Map<String, Object> m = new HashMap<>();
          m.put("id", d.getId());
          m.put("name", d.getName());
          m.put("deploymentTime", d.getDeploymentTime());
          return m;
        })
        .collect(Collectors.toList());
    return ResponseEntity.ok(result);
  }

  @PostMapping("/deploy")
  public ResponseEntity<Map<String, Object>> deploy() {
    Deployment deployment = repositoryService.createDeployment()
        .name("demo-deployment")
        .addClasspathResource("processes/demo-process.bpmn20.xml")
        .deploy();
    Map<String, Object> result = new HashMap<>();
    result.put("id", deployment.getId());
    result.put("name", deployment.getName());
    result.put("deploymentTime", deployment.getDeploymentTime());
    return ResponseEntity.ok(result);
  }

  @PostMapping("/process/start")
  public ResponseEntity<Map<String, Object>> startProcess(@RequestBody(required = false) Map<String, Object> body) {
    String processKey = body != null && body.containsKey("processKey")
        ? body.get("processKey").toString()
        : "demoProcess";
    ProcessInstance pi = runtimeService.startProcessInstanceByKey(processKey);
    Map<String, Object> result = new HashMap<>();
    result.put("processInstanceId", pi.getId());
    result.put("processDefinitionId", pi.getProcessDefinitionId());
    result.put("activityId", pi.getActivityId());
    return ResponseEntity.ok(result);
  }

  @GetMapping("/process/instances")
  public ResponseEntity<List<Map<String, Object>>> listProcessInstances() {
    List<ProcessInstance> list = runtimeService.createProcessInstanceQuery().list();
    List<Map<String, Object>> result = list.stream()
        .map(pi -> {
          Map<String, Object> m = new HashMap<>();
          m.put("id", pi.getId());
          m.put("processDefinitionId", pi.getProcessDefinitionId());
          m.put("activityId", pi.getActivityId());
          return m;
        })
        .collect(Collectors.toList());
    return ResponseEntity.ok(result);
  }
}
