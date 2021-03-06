/*
 *      Copyright (C) 2015 Noorq, Inc.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.noorq.casser.mapping.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.noorq.casser.mapping.annotation.Constraints;
import com.noorq.casser.mapping.annotation.Constraints.NotEmpty;

public final class NotEmptyValidator implements ConstraintValidator<Constraints.NotEmpty, Object>, SizeConstraint {

	@Override
	public void initialize(NotEmpty constraintAnnotation) {
	}

	@Override
	public boolean isValid(Object value, ConstraintValidatorContext context) {
		
		int[] size = getSize(value);
		
		if (size == null) {
			return false;
		}
		
		if (size.length == 0) {
			return true;
		}
		
		return size[0] > 0;
	}

}
