/**
 * Autogenerated by Avro
 *
 * DO NOT EDIT DIRECTLY
 */
package com.findinpath.avro;

import org.apache.avro.specific.SpecificData;
import org.apache.avro.message.BinaryMessageEncoder;
import org.apache.avro.message.BinaryMessageDecoder;
import org.apache.avro.message.SchemaStore;

@SuppressWarnings("all")
@org.apache.avro.specific.AvroGenerated
public class UserCreatedEvent extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  private static final long serialVersionUID = 7153106218350821473L;
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"UserCreatedEvent\",\"namespace\":\"com.findinpath.avro\",\"fields\":[{\"name\":\"userUuid\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}},{\"name\":\"username\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}},{\"name\":\"timestamp\",\"type\":\"long\",\"logicalType\":\"timestamp-millis\"}]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }

  private static SpecificData MODEL$ = new SpecificData();

  private static final BinaryMessageEncoder<UserCreatedEvent> ENCODER =
      new BinaryMessageEncoder<UserCreatedEvent>(MODEL$, SCHEMA$);

  private static final BinaryMessageDecoder<UserCreatedEvent> DECODER =
      new BinaryMessageDecoder<UserCreatedEvent>(MODEL$, SCHEMA$);

  /**
   * Return the BinaryMessageDecoder instance used by this class.
   */
  public static BinaryMessageDecoder<UserCreatedEvent> getDecoder() {
    return DECODER;
  }

  /**
   * Create a new BinaryMessageDecoder instance for this class that uses the specified {@link SchemaStore}.
   * @param resolver a {@link SchemaStore} used to find schemas by fingerprint
   */
  public static BinaryMessageDecoder<UserCreatedEvent> createDecoder(SchemaStore resolver) {
    return new BinaryMessageDecoder<UserCreatedEvent>(MODEL$, SCHEMA$, resolver);
  }

  /** Serializes this UserCreatedEvent to a ByteBuffer. */
  public java.nio.ByteBuffer toByteBuffer() throws java.io.IOException {
    return ENCODER.encode(this);
  }

  /** Deserializes a UserCreatedEvent from a ByteBuffer. */
  public static UserCreatedEvent fromByteBuffer(
      java.nio.ByteBuffer b) throws java.io.IOException {
    return DECODER.decode(b);
  }

   private java.lang.String userUuid;
   private java.lang.String username;
   private long timestamp;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use <code>newBuilder()</code>.
   */
  public UserCreatedEvent() {}

  /**
   * All-args constructor.
   * @param userUuid The new value for userUuid
   * @param username The new value for username
   * @param timestamp The new value for timestamp
   */
  public UserCreatedEvent(java.lang.String userUuid, java.lang.String username, java.lang.Long timestamp) {
    this.userUuid = userUuid;
    this.username = username;
    this.timestamp = timestamp;
  }

  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call.
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return userUuid;
    case 1: return username;
    case 2: return timestamp;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  // Used by DatumReader.  Applications should not call.
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: userUuid = (java.lang.String)value$; break;
    case 1: username = (java.lang.String)value$; break;
    case 2: timestamp = (java.lang.Long)value$; break;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  /**
   * Gets the value of the 'userUuid' field.
   * @return The value of the 'userUuid' field.
   */
  public java.lang.String getUserUuid() {
    return userUuid;
  }

  /**
   * Sets the value of the 'userUuid' field.
   * @param value the value to set.
   */
  public void setUserUuid(java.lang.String value) {
    this.userUuid = value;
  }

  /**
   * Gets the value of the 'username' field.
   * @return The value of the 'username' field.
   */
  public java.lang.String getUsername() {
    return username;
  }

  /**
   * Sets the value of the 'username' field.
   * @param value the value to set.
   */
  public void setUsername(java.lang.String value) {
    this.username = value;
  }

  /**
   * Gets the value of the 'timestamp' field.
   * @return The value of the 'timestamp' field.
   */
  public java.lang.Long getTimestamp() {
    return timestamp;
  }

  /**
   * Sets the value of the 'timestamp' field.
   * @param value the value to set.
   */
  public void setTimestamp(java.lang.Long value) {
    this.timestamp = value;
  }

  /**
   * Creates a new UserCreatedEvent RecordBuilder.
   * @return A new UserCreatedEvent RecordBuilder
   */
  public static com.findinpath.avro.UserCreatedEvent.Builder newBuilder() {
    return new com.findinpath.avro.UserCreatedEvent.Builder();
  }

  /**
   * Creates a new UserCreatedEvent RecordBuilder by copying an existing Builder.
   * @param other The existing builder to copy.
   * @return A new UserCreatedEvent RecordBuilder
   */
  public static com.findinpath.avro.UserCreatedEvent.Builder newBuilder(com.findinpath.avro.UserCreatedEvent.Builder other) {
    return new com.findinpath.avro.UserCreatedEvent.Builder(other);
  }

  /**
   * Creates a new UserCreatedEvent RecordBuilder by copying an existing UserCreatedEvent instance.
   * @param other The existing instance to copy.
   * @return A new UserCreatedEvent RecordBuilder
   */
  public static com.findinpath.avro.UserCreatedEvent.Builder newBuilder(com.findinpath.avro.UserCreatedEvent other) {
    return new com.findinpath.avro.UserCreatedEvent.Builder(other);
  }

  /**
   * RecordBuilder for UserCreatedEvent instances.
   */
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<UserCreatedEvent>
    implements org.apache.avro.data.RecordBuilder<UserCreatedEvent> {

    private java.lang.String userUuid;
    private java.lang.String username;
    private long timestamp;

    /** Creates a new Builder */
    private Builder() {
      super(SCHEMA$);
    }

    /**
     * Creates a Builder by copying an existing Builder.
     * @param other The existing Builder to copy.
     */
    private Builder(com.findinpath.avro.UserCreatedEvent.Builder other) {
      super(other);
      if (isValidValue(fields()[0], other.userUuid)) {
        this.userUuid = data().deepCopy(fields()[0].schema(), other.userUuid);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.username)) {
        this.username = data().deepCopy(fields()[1].schema(), other.username);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.timestamp)) {
        this.timestamp = data().deepCopy(fields()[2].schema(), other.timestamp);
        fieldSetFlags()[2] = true;
      }
    }

    /**
     * Creates a Builder by copying an existing UserCreatedEvent instance
     * @param other The existing instance to copy.
     */
    private Builder(com.findinpath.avro.UserCreatedEvent other) {
            super(SCHEMA$);
      if (isValidValue(fields()[0], other.userUuid)) {
        this.userUuid = data().deepCopy(fields()[0].schema(), other.userUuid);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.username)) {
        this.username = data().deepCopy(fields()[1].schema(), other.username);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.timestamp)) {
        this.timestamp = data().deepCopy(fields()[2].schema(), other.timestamp);
        fieldSetFlags()[2] = true;
      }
    }

    /**
      * Gets the value of the 'userUuid' field.
      * @return The value.
      */
    public java.lang.String getUserUuid() {
      return userUuid;
    }

    /**
      * Sets the value of the 'userUuid' field.
      * @param value The value of 'userUuid'.
      * @return This builder.
      */
    public com.findinpath.avro.UserCreatedEvent.Builder setUserUuid(java.lang.String value) {
      validate(fields()[0], value);
      this.userUuid = value;
      fieldSetFlags()[0] = true;
      return this;
    }

    /**
      * Checks whether the 'userUuid' field has been set.
      * @return True if the 'userUuid' field has been set, false otherwise.
      */
    public boolean hasUserUuid() {
      return fieldSetFlags()[0];
    }


    /**
      * Clears the value of the 'userUuid' field.
      * @return This builder.
      */
    public com.findinpath.avro.UserCreatedEvent.Builder clearUserUuid() {
      userUuid = null;
      fieldSetFlags()[0] = false;
      return this;
    }

    /**
      * Gets the value of the 'username' field.
      * @return The value.
      */
    public java.lang.String getUsername() {
      return username;
    }

    /**
      * Sets the value of the 'username' field.
      * @param value The value of 'username'.
      * @return This builder.
      */
    public com.findinpath.avro.UserCreatedEvent.Builder setUsername(java.lang.String value) {
      validate(fields()[1], value);
      this.username = value;
      fieldSetFlags()[1] = true;
      return this;
    }

    /**
      * Checks whether the 'username' field has been set.
      * @return True if the 'username' field has been set, false otherwise.
      */
    public boolean hasUsername() {
      return fieldSetFlags()[1];
    }


    /**
      * Clears the value of the 'username' field.
      * @return This builder.
      */
    public com.findinpath.avro.UserCreatedEvent.Builder clearUsername() {
      username = null;
      fieldSetFlags()[1] = false;
      return this;
    }

    /**
      * Gets the value of the 'timestamp' field.
      * @return The value.
      */
    public java.lang.Long getTimestamp() {
      return timestamp;
    }

    /**
      * Sets the value of the 'timestamp' field.
      * @param value The value of 'timestamp'.
      * @return This builder.
      */
    public com.findinpath.avro.UserCreatedEvent.Builder setTimestamp(long value) {
      validate(fields()[2], value);
      this.timestamp = value;
      fieldSetFlags()[2] = true;
      return this;
    }

    /**
      * Checks whether the 'timestamp' field has been set.
      * @return True if the 'timestamp' field has been set, false otherwise.
      */
    public boolean hasTimestamp() {
      return fieldSetFlags()[2];
    }


    /**
      * Clears the value of the 'timestamp' field.
      * @return This builder.
      */
    public com.findinpath.avro.UserCreatedEvent.Builder clearTimestamp() {
      fieldSetFlags()[2] = false;
      return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public UserCreatedEvent build() {
      try {
        UserCreatedEvent record = new UserCreatedEvent();
        record.userUuid = fieldSetFlags()[0] ? this.userUuid : (java.lang.String) defaultValue(fields()[0]);
        record.username = fieldSetFlags()[1] ? this.username : (java.lang.String) defaultValue(fields()[1]);
        record.timestamp = fieldSetFlags()[2] ? this.timestamp : (java.lang.Long) defaultValue(fields()[2]);
        return record;
      } catch (java.lang.Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private static final org.apache.avro.io.DatumWriter<UserCreatedEvent>
    WRITER$ = (org.apache.avro.io.DatumWriter<UserCreatedEvent>)MODEL$.createDatumWriter(SCHEMA$);

  @Override public void writeExternal(java.io.ObjectOutput out)
    throws java.io.IOException {
    WRITER$.write(this, SpecificData.getEncoder(out));
  }

  @SuppressWarnings("unchecked")
  private static final org.apache.avro.io.DatumReader<UserCreatedEvent>
    READER$ = (org.apache.avro.io.DatumReader<UserCreatedEvent>)MODEL$.createDatumReader(SCHEMA$);

  @Override public void readExternal(java.io.ObjectInput in)
    throws java.io.IOException {
    READER$.read(this, SpecificData.getDecoder(in));
  }

}