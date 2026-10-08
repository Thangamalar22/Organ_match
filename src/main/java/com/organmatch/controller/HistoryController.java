package com.organmatch.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.organmatch.entity.MatchRecord;
import com.organmatch.service.MatchRunnerService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Controller serving match execution history list, detailed record view, and CSV export.
 */
@Controller
public class HistoryController {

    private final MatchRunnerService matchRunnerService;
    private final ObjectMapper objectMapper;

    public HistoryController(MatchRunnerService matchRunnerService, ObjectMapper objectMapper) {
        this.matchRunnerService = matchRunnerService;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/history")
    public String historyList(
            @RequestParam(required = false) String runBy,
            @RequestParam(required = false) String algorithm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            Model model) {

        Page<MatchRecord> historyPage = matchRunnerService.getHistoryPage(runBy, algorithm, page, size);

        model.addAttribute("historyPage", historyPage);
        model.addAttribute("records", historyPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", historyPage.getTotalPages());
        model.addAttribute("totalElements", historyPage.getTotalElements());

        model.addAttribute("selectedRunBy", runBy);
        model.addAttribute("selectedAlgorithm", algorithm);

        return "history/list";
    }

    @GetMapping("/history/{id}")
    public String historyDetail(@PathVariable Long id, Model model) {
        MatchRecord record = matchRunnerService.getHistoryRecordById(id);
        model.addAttribute("record", record);

        Object resultData = null;
        if (record.getResultJson() != null && !record.getResultJson().isBlank()) {
            try {
                resultData = objectMapper.readValue(record.getResultJson(), Object.class);
            } catch (Exception e) {
                resultData = null;
            }
        }
        model.addAttribute("resultData", resultData);
        model.addAttribute("resultJson", record.getResultJson());

        return "history/detail";
    }

    @GetMapping("/history/export-csv")
    public ResponseEntity<String> exportHistoryCsv(
            @RequestParam(required = false) String runBy,
            @RequestParam(required = false) String algorithm) {

        Page<MatchRecord> historyPage = matchRunnerService.getHistoryPage(runBy, algorithm, 0, 10000);
        String csvData = matchRunnerService.generateHistoryCsv(historyPage.getContent());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=match_history.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvData);
    }
}
