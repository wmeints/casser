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

import com.noorq.casser.mapping.annotation.Constraints.LowerCase;

public final class LowerCaseValidator implements ConstraintValidator<LowerCase, CharSequence> {

	@Override
	public void initialize(LowerCase constraintAnnotation) {
	}

	@Override
	public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
		
		if (value == null) {
			return true;			
		}
		
		final int len = value.length();
		for (int i = 0; i != len; ++i) {
			char c = value.charAt(i);
			if (c <= 0x7F) {
				if (isUpperCaseLetter(c)) {
					return false;
				}
			}
            if (c != Character.toLowerCase(c)) {
                return false;
            }
		}
		
		return true;
	}
	
	private static boolean isUpperCaseLetter(char ch) {
		return ch >= 'A' && ch <= 'Z';
	}

}
