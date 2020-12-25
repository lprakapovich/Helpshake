package com.application.helpshake.model.request;

import com.application.helpshake.model.enums.HelpCategory;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HelpRequest {

    String title;
    String description;
    List<HelpCategory> categoryList;
}
