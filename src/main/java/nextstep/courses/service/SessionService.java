package nextstep.courses.service;

import nextstep.courses.domain.Session;
import nextstep.courses.domain.SessionRepository;
import nextstep.courses.domain.SessionUser;
import nextstep.courses.exception.SessionUserNotFoundException;
import nextstep.users.domain.NextStepUser;
import nextstep.users.domain.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class SessionService {

  private static final String ILLEGAL_SESSION_USER_MESSAGE = "유저가 신청한 세션이 없습니다.";
  private final SessionRepository sessionRepository;
  private final UserRepository userRepository;

  public SessionService(SessionRepository sessionRepository, UserRepository userRepository) {
    this.sessionRepository = sessionRepository;
    this.userRepository = userRepository;
  }

  public Session save(Session session, Long courseId) {
    return sessionRepository.save(session, courseId);
  }

  public Session findById(Long sessionId) {
    return sessionRepository.findById(sessionId);
  }

  public void enrollUser(Long sessionId, String nextStepUserId) {
    Session session = sessionRepository.findById(sessionId);
    NextStepUser nextStepUser = userRepository.findByUserId(nextStepUserId).orElseThrow();

    session.processEnrollment(nextStepUser);
    LocalDateTime currentTime = LocalDateTime.now();
    sessionRepository.saveSessionUser(new SessionUser(session, nextStepUser, currentTime, currentTime));
  }

  public void approveEnrollment(Long sessionId, Long userId) {
    SessionUser sessionUser = sessionRepository.findBySessionIdAndUserId(sessionId, userId);

    if (sessionUser == null) {
      throw new SessionUserNotFoundException(ILLEGAL_SESSION_USER_MESSAGE);
    }

    sessionUser.approve();
    sessionRepository.updateSessionUserStatus(sessionUser);
  }

  public void rejectEnrollment(Long sessionId, Long userId) {
    SessionUser sessionUser = sessionRepository.findBySessionIdAndUserId(sessionId, userId);

    if (sessionUser == null) {
      throw new SessionUserNotFoundException(ILLEGAL_SESSION_USER_MESSAGE);
    }

    sessionUser.reject();
    sessionRepository.updateSessionUserStatus(sessionUser);
  }
}
