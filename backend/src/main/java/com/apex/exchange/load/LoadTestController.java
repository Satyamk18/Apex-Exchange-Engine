package com.apex.exchange.load;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/load-test")
public class LoadTestController {

    private final LoadTestService loadTestService;

    public LoadTestController(LoadTestService loadTestService) {
        this.loadTestService = loadTestService;
    }

    @PostMapping("/run")
    public ResponseEntity<LoadTestResult> runBenchmark(
            @RequestParam(defaultValue = "8") int threads,
            @RequestParam(defaultValue = "10000") int ordersPerThread) {

        LoadTestResult result = loadTestService.executeBenchmark(threads, ordersPerThread);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/results")
    public ResponseEntity<LoadTestResult> getLastResults() {
        LoadTestResult result = loadTestService.getLastResult();
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }
}
