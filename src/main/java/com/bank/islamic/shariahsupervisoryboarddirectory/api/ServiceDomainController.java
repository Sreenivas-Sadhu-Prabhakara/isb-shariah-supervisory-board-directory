package com.bank.islamic.shariahsupervisoryboarddirectory.api;

import com.bank.islamic.shariahsupervisoryboarddirectory.model.ControlRecord;
import com.bank.islamic.shariahsupervisoryboarddirectory.service.ControlRecordStore;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Map;

/**
 * BIAN semantic API for the "Shariah Supervisory Board Directory" service domain.
 *
 * Endpoints follow the BIAN action-term style:
 *   GET  /v1/service-domain                          → who am I (SD metadata)
 *   POST /v1/ssb-member-mandate-directory-entry/initiate                    → Initiate a control record
 *   GET  /v1/ssb-member-mandate-directory-entry                             → Retrieve (list)
 *   GET  /v1/ssb-member-mandate-directory-entry/{crId}/retrieve             → Retrieve (single)
 *   PUT  /v1/ssb-member-mandate-directory-entry/{crId}/update               → Update
 *   PUT  /v1/ssb-member-mandate-directory-entry/{crId}/control              → Control (suspend|resume|terminate)
 */
@RestController
@RequestMapping("/v1")
public class ServiceDomainController {

    private final ControlRecordStore store;

    public ServiceDomainController(ControlRecordStore store) {
        this.store = store;
    }

    @GetMapping("/service-domain")
    public Map<String, String> serviceDomain() {
        return Map.of(
                "serviceDomain", "Shariah Supervisory Board Directory",
                "businessArea", "Shariah Governance and Compliance",
                "businessDomain", "Shariah Governance",
                "functionalPattern", "Catalog",
                "assetType", "SSB Member Mandate",
                "controlRecord", "SSB Member Mandate Directory Entry",
                "version", "0.1.0",
                "phase", "1-shallow"
        );
    }

    @PostMapping("/ssb-member-mandate-directory-entry/initiate")
    @CircuitBreaker(name = "serviceDomain")
    public ResponseEntity<ControlRecord> initiate(@RequestBody(required = false) Map<String, Object> properties) {
        return ResponseEntity.status(HttpStatus.CREATED).body(store.initiate(properties));
    }

    @GetMapping("/ssb-member-mandate-directory-entry")
    public Collection<ControlRecord> list() {
        return store.list();
    }

    @GetMapping("/ssb-member-mandate-directory-entry/{crId}/retrieve")
    public ResponseEntity<ControlRecord> retrieve(@PathVariable String crId) {
        return store.retrieve(crId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/ssb-member-mandate-directory-entry/{crId}/update")
    public ResponseEntity<ControlRecord> update(@PathVariable String crId,
                                                @RequestBody Map<String, Object> properties) {
        return store.update(crId, properties)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/ssb-member-mandate-directory-entry/{crId}/control")
    public ResponseEntity<?> control(@PathVariable String crId,
                                     @RequestBody Map<String, String> body) {
        try {
            return store.control(crId, body.get("action"))
                    .<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
