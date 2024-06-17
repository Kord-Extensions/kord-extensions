/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

import { FieldContextKey, useFieldError, useIsFieldDirty, useIsFieldTouched, useIsFieldValid } from "vee-validate"
import { inject } from "vue"
import { FORM_ITEM_INJECTION_KEY } from "./FormItem.vue"

export function useFormField() {
	const fieldContext = inject(FieldContextKey)
	const fieldItemContext = inject(FORM_ITEM_INJECTION_KEY)

	const fieldState = {
		valid: useIsFieldValid(),
		isDirty: useIsFieldDirty(),
		isTouched: useIsFieldTouched(),
		error: useFieldError(),
	}

	if (!fieldContext)
		throw new Error("useFormField should be used within <FormField>")

	const { name } = fieldContext
	const id = fieldItemContext

	return {
		id,
		name,
		formItemId: `${id}-form-item`,
		formDescriptionId: `${id}-form-item-description`,
		formMessageId: `${id}-form-item-message`,
		...fieldState,
	}
}
