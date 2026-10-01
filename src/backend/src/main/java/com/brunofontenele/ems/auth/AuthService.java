package com.brunofontenele.ems.auth;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import com.brunofontenele.ems.auth.dto.AuthUserDto;
import com.brunofontenele.ems.auth.dto.LoginRequest;
import com.brunofontenele.ems.auth.dto.LoginResponse;
import com.brunofontenele.ems.exceptions.EmsException;
import com.brunofontenele.ems.exceptions.ErrorMessage;
import com.brunofontenele.ems.person.domain.Person;
import com.brunofontenele.ems.person.repository.PersonRepository;
import com.brunofontenele.ems.security.JwtService;
import com.brunofontenele.ems.security.Permission;
import com.brunofontenele.ems.security.RolePermissions;
import com.brunofontenele.ems.security.SecurityUtils;

@Service
public class AuthService {

	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;
	private final PersonRepository personRepository;

	public AuthService(AuthenticationManager authenticationManager, JwtService jwtService,
			PersonRepository personRepository) {
		this.authenticationManager = authenticationManager;
		this.jwtService = jwtService;
		this.personRepository = personRepository;
	}

	public LoginResponse login(LoginRequest request) {
		authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(request.email(), request.password()));

		Person person = personRepository.findByEmail(request.email())
				.orElseThrow(() -> new EmsException(ErrorMessage.INVALID_CREDENTIALS));

		String token = jwtService.generateToken(person);
		return new LoginResponse(token, jwtService.getExpirationMs(), toDto(person, false, null));
	}

	public AuthUserDto currentUser(HttpServletRequest request) {
		Person person = SecurityUtils.currentPerson();
		if (person == null) {
			throw new EmsException(ErrorMessage.NOT_AUTHENTICATED);
		}

		boolean impersonating = false;
		String impersonatorEmail = null;

		Claims claims = parseCurrentToken(request);
		if (claims != null) {
			Object flag = claims.get(JwtService.CLAIM_IMPERSONATING);
			impersonating = flag != null && Boolean.parseBoolean(flag.toString());
			impersonatorEmail = claims.get(JwtService.CLAIM_IMPERSONATOR_EMAIL, String.class);
		}

		return toDto(person, impersonating, impersonatorEmail);
	}

	public LoginResponse impersonate(long targetPersonId) {
		Person admin = SecurityUtils.currentPerson();
		if (admin == null) {
			throw new EmsException(ErrorMessage.NOT_AUTHENTICATED);
		}
		if (admin.getId() != null && admin.getId() == targetPersonId) {
			throw new EmsException(ErrorMessage.CANNOT_IMPERSONATE_SELF);
		}

		Person target = personRepository.findById(targetPersonId)
				.orElseThrow(() -> new EmsException(ErrorMessage.NO_SUCH_PERSON, Long.toString(targetPersonId)));

		Map<String, Object> claims = new HashMap<>();
		claims.put(JwtService.CLAIM_IMPERSONATING, true);
		claims.put(JwtService.CLAIM_IMPERSONATOR_EMAIL, admin.getEmail());
		claims.put(JwtService.CLAIM_IMPERSONATOR_NAME, admin.getName());

		String token = jwtService.generateToken(target, claims);
		return new LoginResponse(token, jwtService.getExpirationMs(), toDto(target, true, admin.getEmail()));
	}

	public LoginResponse stopImpersonation(HttpServletRequest request) {
		Claims claims = parseCurrentToken(request);
		Object flag = claims == null ? null : claims.get(JwtService.CLAIM_IMPERSONATING);
		boolean impersonating = flag != null && Boolean.parseBoolean(flag.toString());
		if (!impersonating) {
			throw new EmsException(ErrorMessage.NOT_IMPERSONATING);
		}

		String impersonatorEmail = claims.get(JwtService.CLAIM_IMPERSONATOR_EMAIL, String.class);
		Person admin = personRepository.findByEmail(impersonatorEmail)
				.orElseThrow(() -> new EmsException(ErrorMessage.NOT_IMPERSONATING));

		String token = jwtService.generateToken(admin);
		return new LoginResponse(token, jwtService.getExpirationMs(), toDto(admin, false, null));
	}

	private Claims parseCurrentToken(HttpServletRequest request) {
		String header = request.getHeader("Authorization");
		if (header == null || !header.startsWith("Bearer ")) {
			return null;
		}
		try {
			return jwtService.extractAllClaims(header.substring(7));
		} catch (Exception e) {
			return null;
		}
	}

	private AuthUserDto toDto(Person person, boolean impersonating, String impersonatorEmail) {
		List<String> permissions = RolePermissions.forRole(person.getType()).stream()
				.map(Permission::name)
				.sorted()
				.toList();
		return new AuthUserDto(
				person.getId(),
				person.getName(),
				person.getEmail(),
				person.getType().name(),
				permissions,
				impersonating,
				impersonatorEmail);
	}
}
