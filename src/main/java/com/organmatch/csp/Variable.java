package com.organmatch.csp;

import com.organmatch.model.OrganUnit;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Represents a CSP variable wrapping an available OrganUnit.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "organUnit")
public class Variable {
    private OrganUnit organUnit;

    @Override
    public String toString() {
        return organUnit != null ? organUnit.getId() : "null";
    }
}
