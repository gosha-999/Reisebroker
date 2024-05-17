import java.util.ArrayList;
import java.util.List;

public class BookingContext {
    private List<BookingRequest> requests;
    private List<String> bookingResults;
    private List<BookingRequest> successfullyBooked;
    private boolean rollbackInitiated;
    private int completedRequests;

    public BookingContext(List<BookingRequest> requests) {
        this.requests = requests;
        this.bookingResults = new ArrayList<>();
        this.successfullyBooked = new ArrayList<>();
        this.rollbackInitiated = false;
        this.completedRequests = 0;
    }

    public List<BookingRequest> getRequests() {
        return requests;
    }

    public List<String> getBookingResults() {
        return bookingResults;
    }

    public void addBookingResult(String result) {
        bookingResults.add(result);
    }

    public List<BookingRequest> getSuccessfullyBooked() {
        return successfullyBooked;
    }

    public void addSuccessfulBooking(BookingRequest request) {
        successfullyBooked.add(request);
    }

    public boolean isRollbackInitiated() {
        return rollbackInitiated;
    }

    public void setRollbackInitiated(boolean rollbackInitiated) {
        this.rollbackInitiated = rollbackInitiated;
    }

    public int getTotalRequests() {
        return requests.size();
    }

    public synchronized void incrementCompletedRequests() {
        completedRequests++;
    }

    public synchronized int getCompletedRequests() {
        return completedRequests;
    }
}
