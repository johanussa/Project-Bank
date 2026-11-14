package org.uni.santo.tomas.helper.validator;

import jakarta.validation.groups.Default;

public interface ValidationGroups {

    interface Post extends Default {}
    interface Put extends Default {}
    interface PutBalance {}
}
