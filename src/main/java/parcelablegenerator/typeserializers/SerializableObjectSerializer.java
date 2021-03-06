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
public class SerializableObjectSerializer extends TypeSerializer {

    public SerializableObjectSerializer(ValueParameterDescriptor field) {
        super(field);
    }

    public String generateReadValue() {
        return "source.readSerializable() as " + getType();
    }

    public String generateWriteValue() {
        return "writeSerializable(" + getFieldName() + ")";
    }
}
