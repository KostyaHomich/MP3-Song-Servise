package com.example.resource.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class DeleteIdsResponse {
    private List<Long> ids;
}
