/*
 * Copyright 2016, Stuart Douglas, and individual contributors as indicated
 * by the @authors tag.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.fakereplace.reflection;

import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.fakereplace.data.AnnotationDataStore;
import org.fakereplace.data.ModifiedMethod;

public class AnnotationReflection {

    private static boolean heiracheyChanged(Class<?> clazz) {
        Class<?> c = clazz;
        while (c != Object.class && c != null) {
            if (AnnotationDataStore.isClassDataRecorded(c)) {
                return true;
            }
            c = c.getSuperclass();
        }
        return false;
    }

    public static boolean isAnnotationPresent(Class<?> clazz, Class anType) {
        if (anType.isAnnotationPresent(Inherited.class) && heiracheyChanged(clazz)) {
            Class<?> c = clazz;
            while (c != null && c != Object.class) {
                if (AnnotationDataStore.isClassDataRecorded(c)) {
                    if (AnnotationDataStore.isClassAnnotationPresent(c, anType)) {
                        return true;
                    }
                } else {
                    // can't just use getAnnotation, as an inherited annotation
                    // may have been removed from a parent class
                    Annotation[] declared = c.getDeclaredAnnotations();
                    for (Annotation a : declared) {
                        if (a.annotationType() == anType) {
                            return true;
                        }
                    }
                }
                c = c.getSuperclass();
            }
            return false;
        } else if (AnnotationDataStore.isClassDataRecorded(clazz)) {
            return AnnotationDataStore.isClassAnnotationPresent(clazz, anType);
        }
        return clazz.isAnnotationPresent(anType);
    }

    public static Annotation getAnnotation(Class<?> clazz, Class anType) {
        if (anType.isAnnotationPresent(Inherited.class) && heiracheyChanged(clazz)) {
            Annotation result = null;
            Class<?> c = clazz;
            while (result == null && c != null && c != Object.class) {
                if (AnnotationDataStore.isClassDataRecorded(c)) {
                    result = AnnotationDataStore.getClassAnnotation(c, anType);
                } else {
                    // can't just use getAnnotation, as an inherited annotation
                    // may have been removed from a parent class
                    Annotation[] declared = c.getDeclaredAnnotations();
                    for (Annotation a : declared) {
                        if (a.annotationType() == anType) {
                            result = a;
                            break;
                        }
                    }
                }
                c = c.getSuperclass();
            }
            return result;
        } else if (AnnotationDataStore.isClassDataRecorded(clazz)) {
            return AnnotationDataStore.getClassAnnotation(clazz, anType);
        }
        return clazz.getAnnotation(anType);
    }

    public static Annotation[] getAnnotations(Class<?> clazz) {
        if (heiracheyChanged(clazz)) {
            Annotation[] pres;
            if (AnnotationDataStore.isClassDataRecorded(clazz)) {
                pres = AnnotationDataStore.getClassAnnotations(clazz);
            } else {
                pres = clazz.getDeclaredAnnotations();
            }
            List<Annotation> result = new ArrayList<Annotation>();
            for (Annotation a : pres) {
                result.add(a);
            }
            Class<?> c = clazz.getSuperclass();
            while (c != Object.class && c != null) {
                if (AnnotationDataStore.isClassDataRecorded(c)) {
                    pres = AnnotationDataStore.getClassAnnotations(c);
                } else {
                    pres = c.getDeclaredAnnotations();
                }
                for (Annotation a : pres) {
                    if (a.annotationType().isAnnotationPresent(Inherited.class)) {
                        result.add(a);
                    }
                }
                c = c.getSuperclass();
            }
            Annotation[] ret = new Annotation[result.size()];
            int count = 0;
            for (Annotation a : result) {
                ret[count++] = a;
            }
            return ret;
        }
        return clazz.getAnnotations();
    }

    public static Annotation[] getDeclaredAnnotations(Class<?> clazz) {
        if (AnnotationDataStore.isClassDataRecorded(clazz)) {
            Annotation[] result = AnnotationDataStore.getClassAnnotations(clazz);
            return result;
        }
        return clazz.getDeclaredAnnotations();
    }

    public static boolean isAnnotationPresent(Field clazz, Class anType) {
        if (AnnotationDataStore.isFieldDataRecorded(clazz)) {
            boolean result = AnnotationDataStore.isFieldAnnotationPresent(clazz, anType);

            return result;
        }
        return clazz.isAnnotationPresent(anType);
    }

    public static Annotation getAnnotation(Field clazz, Class anType) {
        if (AnnotationDataStore.isFieldDataRecorded(clazz)) {
            Annotation result = AnnotationDataStore.getFieldAnnotation(clazz, anType);

            return result;
        }
        return clazz.getAnnotation(anType);
    }

    public static Annotation[] getAnnotations(Field clazz) {
        if (AnnotationDataStore.isFieldDataRecorded(clazz)) {
            Annotation[] result = AnnotationDataStore.getFieldAnnotations(clazz);
            return result;
        }
        return clazz.getAnnotations();
    }

    public static Annotation[] getDeclaredAnnotations(Field clazz) {
        if (AnnotationDataStore.isFieldDataRecorded(clazz)) {
            Annotation[] result = AnnotationDataStore.getFieldAnnotations(clazz);
            return result;
        }
        return clazz.getDeclaredAnnotations();
    }

    public static boolean isAnnotationPresent(Method clazz, Class anType) {
        if (AnnotationDataStore.isMethodDataRecorded(clazz)) {
            boolean result = AnnotationDataStore.isMethodAnnotationPresent(clazz, anType);

            return result;
        }
        return clazz.isAnnotationPresent(anType);
    }

    public static Annotation getAnnotation(Method clazz, Class anType) {
        if (AnnotationDataStore.isMethodDataRecorded(clazz)) {
            Annotation result = AnnotationDataStore.getMethodAnnotation(clazz, anType);

            return result;
        }
        return clazz.getAnnotation(anType);
    }

    public static Annotation[] getAnnotations(Method clazz) {
        if (AnnotationDataStore.isMethodDataRecorded(clazz)) {
            Annotation[] result = AnnotationDataStore.getMethodAnnotations(clazz);

            int rc = 0;
            boolean found = false;
            for (Annotation a : result) {
                if (a instanceof ModifiedMethod) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return result;
            }
            Annotation[] ret = new Annotation[result.length - 1];
            for (Annotation a : result) {
                if (!(a instanceof ModifiedMethod)) {
                    ret[rc] = a;
                    rc++;
                }
            }
            return ret;
        }
        if (clazz.isAnnotationPresent(ModifiedMethod.class)) {
            Annotation[] d = clazz.getAnnotations();
            Annotation[] ret = new Annotation[d.length - 1];
            int rc = 0;
            for (Annotation a : d) {
                if (!(a instanceof ModifiedMethod)) {
                    ret[rc] = a;
                    rc++;
                }
            }
            return ret;
        }
        return clazz.getAnnotations();
    }

    public static Annotation[] getDeclaredAnnotations(Method clazz) {
        if (AnnotationDataStore.isMethodDataRecorded(clazz)) {
            Annotation[] result = AnnotationDataStore.getMethodAnnotations(clazz);

            int rc = 0;
            boolean found = false;
            for (Annotation a : result) {
                if (a instanceof ModifiedMethod) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return result;
            }
            Annotation[] ret = new Annotation[result.length - 1];
            for (Annotation a : result) {
                if (!(a instanceof ModifiedMethod)) {
                    ret[rc] = a;
                    rc++;
                }
            }
            return ret;
        }
        if (clazz.isAnnotationPresent(ModifiedMethod.class)) {
            Annotation[] d = clazz.getDeclaredAnnotations();
            Annotation[] ret = new Annotation[d.length - 1];
            int rc = 0;
            for (Annotation a : d) {
                if (!(a instanceof ModifiedMethod)) {
                    ret[rc] = a;
                    rc++;
                }
            }
            return ret;
        }
        return clazz.getDeclaredAnnotations();
    }

    public static Annotation[][] getParameterAnnotations(Method clazz) {
        if (AnnotationDataStore.isMethodDataRecorded(clazz)) {
            Annotation[][] result = AnnotationDataStore.getMethodParameterAnnotations(clazz);
            return result;
        }
        return clazz.getParameterAnnotations();
    }

    // constructors

    public static boolean isAnnotationPresent(Constructor<?> clazz, Class anType) {
        if (AnnotationDataStore.isConstructorDataRecorded(clazz)) {
            boolean result = AnnotationDataStore.isConstructorAnnotationPresent(clazz, anType);

            return result;
        }
        return clazz.isAnnotationPresent(anType);
    }

    public static Annotation getAnnotation(Constructor<?> clazz, Class anType) {
        if (AnnotationDataStore.isConstructorDataRecorded(clazz)) {
            Annotation result = AnnotationDataStore.getConstructorAnnotation(clazz, anType);

            return result;
        }
        return clazz.getAnnotation(anType);
    }

    public static Annotation[] getAnnotations(Constructor<?> clazz) {
        if (AnnotationDataStore.isConstructorDataRecorded(clazz)) {
            Annotation[] result = AnnotationDataStore.getConstructorAnnotations(clazz);
            return result;
        }
        return clazz.getAnnotations();
    }

    public static Annotation[] getDeclaredAnnotations(Constructor<?> clazz) {
        if (AnnotationDataStore.isConstructorDataRecorded(clazz)) {
            Annotation[] result = AnnotationDataStore.getConstructorAnnotations(clazz);
            return result;
        }
        return clazz.getDeclaredAnnotations();
    }

    public static Annotation[][] getParameterAnnotations(Constructor<?> clazz) {
        if (AnnotationDataStore.isConstructorDataRecorded(clazz)) {
            Annotation[][] result = AnnotationDataStore.getMethodParameterAnnotations(clazz);
            return result;
        }
        return clazz.getParameterAnnotations();
    }

    // AnnotatedElement

    public static boolean isAnnotationPresent(AnnotatedElement clazz, Class anType) {
        if (clazz instanceof Class<?>) {
            return isAnnotationPresent((Class) clazz, anType);
        } else if (clazz instanceof Field) {
            return isAnnotationPresent((Field) clazz, anType);
        } else if (clazz instanceof Method) {
            return isAnnotationPresent((Method) clazz, anType);
        } else if (clazz instanceof Constructor<?>) {
            return isAnnotationPresent((Constructor<?>) clazz, anType);
        }
        return clazz.isAnnotationPresent(anType);
    }

    public static Annotation getAnnotation(AnnotatedElement clazz, Class anType) {
        if (clazz instanceof Class<?>) {
            return getAnnotation((Class) clazz, anType);
        } else if (clazz instanceof Field) {
            return getAnnotation((Field) clazz, anType);
        } else if (clazz instanceof Method) {
            return getAnnotation((Method) clazz, anType);
        } else if (clazz instanceof Constructor<?>) {
            return getAnnotation((Constructor<?>) clazz, anType);
        }
        return clazz.getAnnotation(anType);
    }

    public static Annotation[] getAnnotations(AnnotatedElement clazz) {
        if (clazz instanceof Class<?>) {
            return getAnnotations((Class) clazz);
        } else if (clazz instanceof Field) {
            return getAnnotations((Field) clazz);
        } else if (clazz instanceof Method) {
            return getAnnotations((Method) clazz);
        } else if (clazz instanceof Constructor<?>) {
            return getAnnotations((Constructor<?>) clazz);
        }
        return clazz.getAnnotations();
    }

    public static Annotation[] getDeclaredAnnotations(AnnotatedElement clazz) {
        if (clazz instanceof Class<?>) {
            return getDeclaredAnnotations((Class) clazz);
        } else if (clazz instanceof Field) {
            return getDeclaredAnnotations((Field) clazz);
        } else if (clazz instanceof Method) {
            return getDeclaredAnnotations((Method) clazz);
        } else if (clazz instanceof Constructor<?>) {
            return getDeclaredAnnotations((Constructor<?>) clazz);
        }
        return clazz.getDeclaredAnnotations();
    }

}
