package com.organmatch.model;

/**
 * Enum representing ABO Blood Groups and compatibility matching.
 */
public enum BloodGroup {
    O,
    A,
    B,
    AB;

    /**
     * Checks standard ABO compatibility for organ donation.
     * 
     * @param recipient The recipient's blood group
     * @return true if donor's blood group is compatible with recipient's blood group
     */
    public boolean canDonateTo(BloodGroup recipient) {
        if (recipient == null) {
            return false;
        }
        if (this == O) {
            return true;
        }
        if (this == recipient) {
            return true;
        }
        return recipient == AB;
    }
}
