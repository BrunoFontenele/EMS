package com.brunofontenele.ems.security;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import com.brunofontenele.ems.person.domain.Person.PersonType;

/**
 * Single source of truth mapping each role (PersonType) to the permissions it
 * grants. Change the grants here to change what a role can do.
 */
public final class RolePermissions {

	private static final Map<PersonType, Set<Permission>> ROLE_PERMISSIONS = new EnumMap<>(PersonType.class);

	static {
		// Administrators can do everything, including impersonation.
		ROLE_PERMISSIONS.put(PersonType.ADMINISTRATOR, EnumSet.allOf(Permission.class));

		// School staff manage people but cannot delete or impersonate.
		ROLE_PERMISSIONS.put(PersonType.SCHOOL_STAFF, EnumSet.of(
				Permission.PERSON_READ,
				Permission.SCHOOL_READ,
				Permission.SUBJECT_READ,
				Permission.EXAM_READ,
				Permission.EXAM_CREATE,
				Permission.QUESTION_READ,
				Permission.QUESTION_CREATE,
				Permission.QUESTION_UPDATE,
				Permission.QUESTION_DELETE,
				Permission.STATISTICS_READ));

		// Teachers can read people and statistics.
		ROLE_PERMISSIONS.put(PersonType.TEACHER, EnumSet.of(
				Permission.QUESTION_READ,
				Permission.CORRECTION_READ,
				Permission.CORRECTION_UPDATE,
				Permission.REVIEW_READ,
				Permission.REVIEW_UPDATE));

		// Students can read the people directory.
		ROLE_PERMISSIONS.put(PersonType.STUDENT, EnumSet.of(
				Permission.EXAM_READ,
				Permission.QUESTION_READ,
				Permission.REVIEW_READ,
				Permission.REVIEW_CREATE));
	}

	private RolePermissions() {
	}

	public static Set<Permission> forRole(PersonType type) {
		return Collections.unmodifiableSet(
				ROLE_PERMISSIONS.getOrDefault(type, EnumSet.noneOf(Permission.class)));
	}
}
