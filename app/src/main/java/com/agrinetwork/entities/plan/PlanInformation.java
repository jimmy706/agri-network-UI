package com.agrinetwork.entities.plan;

import com.agrinetwork.entities.User;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PlanInformation extends Plan {
    private User owner;
}
