package com.agrinetwork.helpers;

import com.agrinetwork.entities.Attribute;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Setter
public class AttributesConverter {
    private final List<Attribute> attributes;

    public Map<String, String> toMap() {
        Map<String, String> result = new HashMap<>();

        if(attributes != null && !attributes.isEmpty()) {
            for(Attribute attr: attributes) {
                if(!result.containsKey(attr.getName())) {
                    result.put(attr.getName(), attr.getValue());
                }
            }
        }

        return result;
    }
}
