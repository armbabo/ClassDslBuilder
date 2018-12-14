/*
 * Copyright (C) 2016 Nekocode (https://github.com/nekocode)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package parcelablegenerator.typeserializers;

import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor;

/**
 * @author nekocode (nekocode.cn@gmail.com)
 */
public class EnumSerializer extends TypeSerializer {

    public EnumSerializer(ValueParameterDescriptor field) {
        super(field);
    }

    public String generateReadValue() {
        if (!isTypeNullable()) {
            return getNoneNullType() + ".values()[source.readInt()]";
        } else {
            return "source.readValue(Int::class.java.classLoader)?.let { " + getNoneNullType() + ".values()[it as Int] }";
        }
    }

    public String generateWriteValue() {
        if (!isTypeNullable()) {
            return "writeInt(" + getFieldName() + ".ordinal)";
        } else {
            return "writeValue(" + getFieldName() + (isTypeNullable() ? "?" : "") + ".ordinal)";
        }
    }
}
