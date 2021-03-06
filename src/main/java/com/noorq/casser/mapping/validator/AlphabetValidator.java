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

import java.util.Arrays;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.noorq.casser.mapping.annotation.Constraints.Alphabet;

public final class AlphabetValidator implements ConstraintValidator<Alphabet, CharSequence> {

	char[] alphabet;
	
	@Override
	public void initialize(Alphabet constraintAnnotation) {
		alphabet = constraintAnnotation.value().toCharArray();
		Arrays.sort(alphabet);
	}

	@Override
	public boolean isValid(CharSequence value,
			ConstraintValidatorContext context) {

		if (value == null) {
			return true;			
		}
		
		final int len = value.length();
		for (int i = 0; i != len; ++i) {
			
			char ch = value.charAt(i);
			
			if (Arrays.binarySearch(alphabet, ch) < 0) {
				return false;
			}

		}
		
		return true;
	}

}
