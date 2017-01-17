package org.openlmis.referencedata.util.messagekeys;

public abstract class SupervisoryNodeMessageKeys extends MessageKeys {
  private static final String ERROR = join(SERVICE_ERROR, SUPERVISORY_NODE);

  public static final String ERROR_NON_EXISTENT = join(ERROR, NON_EXISTENT);
  public static final String ERROR_NOT_FOUND_WITH_PROGRAM_AND_FACILITY =
      join(ERROR, NOT_FOUND, WITH, PROGRAM, AND, FACILITY);
}
