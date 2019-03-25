package eu.tib.profileservice.connector;

public class ConnectorException extends Exception {

  private static final long serialVersionUID = 6715456731691304091L;

  /**
   * Constructor of {@link ConnectorException}.
   *
   * @param msg message
   * @param e exception
   */
  public ConnectorException(final String msg, final Exception e) {
    super(msg, e);
  }

  /**
   * Constructor of {@link ConnectorException}.
   *
   * @param msg message
   */
  public ConnectorException(final String msg) {
    super(msg);
  }

}
