/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

export { getObjectFormSchema, getBaseSchema, getBaseType } from "./utils"
export type { Config, ConfigItem, FieldProps } from "./interface"

export { default as AutoForm } from "./AutoForm.vue"
export { default as AutoFormField } from "./AutoFormField.vue"
export { default as AutoFormLabel } from "./AutoFormLabel.vue"

export { default as AutoFormFieldArray } from "./AutoFormFieldArray.vue"
export { default as AutoFormFieldBoolean } from "./AutoFormFieldBoolean.vue"
export { default as AutoFormFieldDate } from "./AutoFormFieldDate.vue"
export { default as AutoFormFieldEnum } from "./AutoFormFieldEnum.vue"
export { default as AutoFormFieldFile } from "./AutoFormFieldFile.vue"
export { default as AutoFormFieldInput } from "./AutoFormFieldInput.vue"
export { default as AutoFormFieldNumber } from "./AutoFormFieldNumber.vue"
export { default as AutoFormFieldObject } from "./AutoFormFieldObject.vue"
