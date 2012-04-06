package com.villemos.ispace.core.utilities;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public class IoFieldSetter {

	public synchronized static void setField(Object io, String fieldName, Object value) {
		try {
			/** If this class has such a field, set it. If not, then an exception
			 * is thrown. */
			Field field = io.getClass().getField(fieldName);
			
			/** See if the field is a List*/
			if (field.get(io) instanceof List) {
				Method add = List.class.getDeclaredMethod("add",Object.class);
				add.invoke(field.get(io), value);
			}
		}
		catch (Exception e) {
			/** The field does not exist. Set it as a dynamic field. Notice that the 
			 * dynamic field is a MultiMap, i.e. multiple entries may have the same
			 * key. */

		}
	}
}
