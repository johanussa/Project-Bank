package org.uni.santo.tomas.helper.validator;

import jakarta.validation.groups.Default;

public interface ValidationGroups {

    interface Deposit extends Default {}
    interface Withdraw extends Default {}
    interface Transfer extends Default {}
}
