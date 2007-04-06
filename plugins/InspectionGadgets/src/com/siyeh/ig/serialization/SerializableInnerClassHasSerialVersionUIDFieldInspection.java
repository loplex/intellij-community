/*
 * Copyright 2003-2007 Dave Griffith, Bas Leijdekkers
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.siyeh.ig.serialization;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiModifier;
import com.siyeh.HardcodedMethodConstants;
import com.siyeh.InspectionGadgetsBundle;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.InspectionGadgetsFix;
import com.siyeh.ig.fixes.AddSerialVersionUIDFix;
import com.siyeh.ig.psiutils.SerializationUtils;
import org.jetbrains.annotations.NotNull;

public class SerializableInnerClassHasSerialVersionUIDFieldInspection
        extends SerializableInspection {

    @NotNull
    public String getID() {
        return "SerializableNonStaticInnerClassWithoutSerialVersionUID";
    }

    @NotNull
    public String getDisplayName() {
        return InspectionGadgetsBundle.message(
                "serializable.inner.class.has.serial.version.uid.field.display.name");
    }

    @NotNull
    protected String buildErrorString(Object... infos) {
        return InspectionGadgetsBundle.message(
                "serializable.inner.class.has.serial.version.uid.field.problem.descriptor");
    }

    protected InspectionGadgetsFix buildFix(PsiElement location) {
        return new AddSerialVersionUIDFix();
    }

    public BaseInspectionVisitor buildVisitor() {
        return new SerializableInnerClassHasSerialVersionUIDFieldVisitor();
    }

    private class SerializableInnerClassHasSerialVersionUIDFieldVisitor
            extends BaseInspectionVisitor {

        public void visitClass(@NotNull PsiClass aClass) {
            // no call to super, so it doesn't drill down
            if (aClass.isInterface() || aClass.isAnnotationType() ||
                    aClass.isEnum()) {
                return;
            }
            if (hasSerialVersionUIDField(aClass)) {
                return;
            }
            final PsiClass containingClass = aClass.getContainingClass();
            if (containingClass == null) {
                return;
            }
            if (aClass.hasModifierProperty(PsiModifier.STATIC)) {
                return;
            }
            if (!SerializationUtils.isSerializable(aClass)) {
                return;
            }
            if (isIgnoredSubclass(aClass)) {
                return;
            }
            registerClassError(aClass);
        }

        private boolean hasSerialVersionUIDField(PsiClass aClass) {
            final PsiField[] fields = aClass.getFields();
            boolean hasSerialVersionUID = false;
            for (PsiField field : fields) {
                final String fieldName = field.getName();
                if (HardcodedMethodConstants.SERIAL_VERSION_UID.equals(
                        fieldName)) {
                    hasSerialVersionUID = true;
                }
            }
            return hasSerialVersionUID;
        }
    }
}