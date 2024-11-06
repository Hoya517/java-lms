package nextstep.courses.domain;

import nextstep.courses.CannotOpenException;
import nextstep.users.domain.NsUser;

import java.util.Objects;

public class Session {
    private final Long id;

    private final Long courseId;

    private final SessionPeriod period;

    private final SessionCoverImage coverImage;

    private final SessionFeeType feeType;

    private final SessionAmount amount;

    private final int maxPersonnel;

    private SessionStatus status;

    public Session(Long courseId, SessionPeriod period, SessionCoverImage coverImage, SessionFeeType feeType, SessionAmount amount, int maxPersonnel, SessionStatus status) {
        this(null, courseId, period, coverImage, feeType, amount, maxPersonnel, status);
    }

    public Session(Long id, Long courseId, SessionPeriod period, SessionCoverImage coverImage, SessionFeeType feeType, SessionAmount amount, int maxPersonnel, SessionStatus status) {
        this.id = id;
        this.courseId = courseId;
        this.period = period;
        this.coverImage = coverImage;
        this.feeType = feeType;
        this.amount = amount;
        this.maxPersonnel = maxPersonnel;
        this.status = status;
    }

    public static Session paidSession(Long id, Long courseId, SessionPeriod period, SessionCoverImage coverImage, SessionAmount amount, int maxPersonnel, SessionStatus status) {
        validPaidSessionAmount(amount);
        return new Session(id, courseId, period, coverImage, SessionFeeType.PAID, amount, maxPersonnel, status);
    }

    private static void validPaidSessionAmount(SessionAmount amount) {
        if (Objects.equals(amount, new SessionAmount(0))) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
    }

    public static Session freeSession(Long id, Long courseId, SessionPeriod period, SessionCoverImage coverImage, SessionStatus status) {
        return new Session(id, courseId, period, coverImage, SessionFeeType.FREE, new SessionAmount(0L), 0, status);
    }

    public static Session from(SessionCreate sessionCreate) {
        return new Session(
                sessionCreate.getCourseId(),
                new SessionPeriod(sessionCreate.getStartDate(), sessionCreate.getEndDate()),
                new SessionCoverImage(sessionCreate.getFileSize(), sessionCreate.getFileType(), sessionCreate.getWidth(), sessionCreate.getHeight()),
                sessionCreate.getAmount() == 0L ? SessionFeeType.FREE : SessionFeeType.PAID,
                new SessionAmount(sessionCreate.getAmount()),
                sessionCreate.getMaxPersonnel(),
                SessionStatus.PREPARING);
    }

    public void open() {
        if (!SessionStatus.PREPARING.equals(this.status)) {
            throw new CannotOpenException("It cannot be opened." + this.status);
        }
        this.status = SessionStatus.RECRUITING;
    }

    public Student apply(NsUser nsUser, SessionApply addInfo) {
        validStatus();
        validAmount(addInfo.getAmount());
        validMaxPersonnel(addInfo.getCountPersonnel());
        return new Student(nsUser.getId(), this.id);
    }

    private void validMaxPersonnel(int countPersonnel) {
        if (feeType.isPaid() && countPersonnel >= maxPersonnel) {
            throw new IllegalArgumentException("Max personnel exceeded.");
        }
    }

    private void validAmount(SessionAmount amount) {
        if (!Objects.equals(amount, this.amount)) {
            throw new IllegalArgumentException("Payment amount does not match.");
        }
    }

    private void validStatus() {
        if (!status.isRecruiting()) {
            throw new IllegalArgumentException("Session is not recruiting.");
        }
    }

    public Long getId() {
        return id;
    }

    public Long getCourseId() {
        return courseId;
    }

    public SessionPeriod getPeriod() {
        return period;
    }

    public SessionCoverImage getCoverImage() {
        return coverImage;
    }

    public SessionFeeType getFeeType() {
        return feeType;
    }

    public SessionAmount getAmount() {
        return amount;
    }

    public int getMaxPersonnel() {
        return maxPersonnel;
    }

    public SessionStatus getStatus() {
        return status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Session session = (Session) o;
        return maxPersonnel == session.maxPersonnel && Objects.equals(id, session.id) && Objects.equals(courseId, session.courseId) && Objects.equals(period, session.period) && Objects.equals(coverImage, session.coverImage) && feeType == session.feeType && Objects.equals(amount, session.amount) && status == session.status;
    }
}