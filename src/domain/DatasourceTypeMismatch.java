package domain;

/**
 * Use this any time the gateways type doesn't match what you expect.  Notice that it extends Error instead of
 * Exception.  TODO figure out why it is an Error instead of an Exception
 */
public class DatasourceTypeMismatch extends Error {
}
