package org.eclipse.uprotocol.transport.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.uprotocol.v1.UAttributes;
import org.eclipse.uprotocol.v1.UMessage;
import org.eclipse.uprotocol.v1.UMessageType;
import org.eclipse.uprotocol.v1.UPriority;
import org.eclipse.uprotocol.v1.UUID;
import org.eclipse.uprotocol.transport.builder.UMessageBuilder;
import org.eclipse.uprotocol.transport.validate.UAttributesValidator;
import org.eclipse.uprotocol.v1.UUri;
import org.eclipse.uprotocol.validation.ValidationResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class UAttributeValidatorTest {

    private UUri buildDefaultUUri() {
        return UUri.newBuilder().setUeId(1).setUeVersionMajor(1).setResourceId(0).build();
    }

    private UUri buildMethodUUri() {
        return UUri.newBuilder().setUeId(1).setUeVersionMajor(1).setResourceId(1).build();
    }

    private UUri buildTopicUUri() {
        return UUri.newBuilder().setUeId(1).setUeVersionMajor(1).setResourceId(0x8000).build();
    }

    @Test
    @DisplayName("Test creating a UMessage of type publish then validating it using UAttributeValidator for the happy path")
    public void testUAttributeValidatorHappyPath() {
        UMessage message = UMessageBuilder.publish(buildTopicUUri()).build();

        UAttributesValidator validator = UAttributesValidator.getValidator(message.getAttributes());
        ValidationResult result = validator.validate(message.getAttributes());
        assertTrue(result.isSuccess());
        assertEquals(validator.toString(), "UAttributesValidator.Publish");
    }

    @Test
    @DisplayName("Test validation a notification message using UAttributeValidator")
    public void testUAttributeValidatorNotification() {
        UMessage message = UMessageBuilder.notification(buildTopicUUri(), buildDefaultUUri()).build();

        UAttributesValidator validator = UAttributesValidator.getValidator(message.getAttributes());
        ValidationResult result = validator.validate(message.getAttributes());
        assertTrue(result.isSuccess());
        assertEquals(validator.toString(), "UAttributesValidator.Notification");
    }

    @Test
    @DisplayName("Test validation a request message using UAttributeValidator")
    public void testUAttributeValidatorRequest() {
        UMessage message = UMessageBuilder.request(buildDefaultUUri(), buildMethodUUri(), 1000).build();

        UAttributesValidator validator = UAttributesValidator.getValidator(message.getAttributes());
        ValidationResult result = validator.validate(message.getAttributes());
        assertTrue(result.isSuccess());
        assertEquals(validator.toString(), "UAttributesValidator.Request");
    }

    @Test
    @DisplayName("Test validation a response message using UAttributeValidator")
    public void testUAttributeValidatorResponse() {
        UMessage request = UMessageBuilder.request(buildDefaultUUri(), buildMethodUUri(), 1000).build();
        UMessage response = UMessageBuilder.response(buildMethodUUri(),buildDefaultUUri(), request.getAttributes().getId()).build();

        UAttributesValidator validator = UAttributesValidator.getValidator(response.getAttributes());
        ValidationResult result = validator.validate(response.getAttributes());
        assertTrue(result.isSuccess());
        assertEquals(validator.toString(), "UAttributesValidator.Response");
        assertEquals(result.getMessage(), "");
    }

    @Test
    @DisplayName("Test validation a response message using UAttributeValidator when passed request UAttributes")
    public void testUAttributeValidatorResponseWithRequestAttributes() {
        UMessage request = UMessageBuilder.request(buildDefaultUUri(), buildMethodUUri(), 1000).build();
        UMessage response = UMessageBuilder.response(request.getAttributes()).build();

        UAttributesValidator validator = UAttributesValidator.getValidator(response.getAttributes());
        ValidationResult result = validator.validate(response.getAttributes());
        assertTrue(result.isSuccess());
        assertEquals(validator.toString(), "UAttributesValidator.Response");
    }

    @Test
    @DisplayName("Test validation failed when using the publish validator to test request messages")
    public void testUAttributeValidatorRequestWithPublishValidator() {
        UMessage message = UMessageBuilder.request(buildDefaultUUri(), buildMethodUUri(), 1000).build();

        UAttributesValidator validator = UAttributesValidator.Validators.PUBLISH.validator();
        ValidationResult result = validator.validate(message.getAttributes());
        assertTrue(result.isFailure());
        assertEquals(validator.toString(), "UAttributesValidator.Publish");
        assertEquals(result.getMessage(), "Wrong Attribute Type [UMESSAGE_TYPE_REQUEST],Sink should not be present");
    }

    @Test
    @DisplayName("Test validation failed when using the notification validator to test publish messages")
    public void testUAttributeValidatorPublishWithNotificationValidator() {
        UMessage message = UMessageBuilder.publish(buildTopicUUri()).build();

        UAttributesValidator validator = UAttributesValidator.Validators.NOTIFICATION.validator();
        ValidationResult result = validator.validate(message.getAttributes());
        assertTrue(result.isFailure());
        assertEquals(validator.toString(), "UAttributesValidator.Notification");
        assertEquals(result.getMessage(), "Wrong Attribute Type [UMESSAGE_TYPE_PUBLISH],Missing Sink");
    }

    @Test
    @DisplayName("Test validation failed when using the request validator to test response messages")
    public void testUAttributeValidatorResponseWithRequestValidator() {
        UMessage request = UMessageBuilder.request(buildDefaultUUri(), buildMethodUUri(), 1000).build();
        UMessage response = UMessageBuilder.response(request.getAttributes()).build();

        UAttributesValidator validator = UAttributesValidator.Validators.REQUEST.validator();
        ValidationResult result = validator.validate(response.getAttributes());
        assertTrue(result.isFailure());
        assertEquals(validator.toString(), "UAttributesValidator.Request");
        assertEquals(result.getMessage(), "Wrong Attribute Type [UMESSAGE_TYPE_RESPONSE],Missing TTL,Invalid Sink Uri,Message should not have a reqid");
    }

    @Test
    @DisplayName("Test validation failed when using the response validator to test notification messages")
    public void testUAttributeValidatorNotificationWithResponseValidator() {
        UMessage message = UMessageBuilder.notification(buildTopicUUri(), buildDefaultUUri()).build();

        UAttributesValidator validator = UAttributesValidator.Validators.RESPONSE.validator();
        ValidationResult result = validator.validate(message.getAttributes());
        assertTrue(result.isFailure());
        assertEquals(validator.toString(), "UAttributesValidator.Response");
        assertEquals(result.getMessage(), "Wrong Attribute Type [UMESSAGE_TYPE_NOTIFICATION],Invalid UPriority [UPRIORITY_CS1],Missing correlationId");
    }

    @Test
    @DisplayName("Test validation of request message has an invalid sink attribute")
    public void testUAttributeValidatorRequestMissingSink() {
        UMessage message = UMessageBuilder.request(buildDefaultUUri(), buildDefaultUUri(), 1000).build();

        UAttributesValidator validator = UAttributesValidator.getValidator(message.getAttributes());
        ValidationResult result = validator.validate(message.getAttributes());
        assertTrue(result.isFailure());
        assertEquals(validator.toString(), "UAttributesValidator.Request");
        assertEquals(result.getMessage(), "Invalid Sink Uri");
    }
    
    @Test
    @DisplayName("Test validation of request message that has a permission level that is less than 0")
    public void testUAttributeValidatorRequestInvalidPermissionLevel() {
        UMessage message = UMessageBuilder.request(buildDefaultUUri(), buildMethodUUri(), 1000)
        .withPermissionLevel(-1).build();

        UAttributesValidator validator = UAttributesValidator.getValidator(message.getAttributes());
        ValidationResult result = validator.validate(message.getAttributes());
        assertTrue(result.isFailure());
        assertEquals(validator.toString(), "UAttributesValidator.Request");
        assertEquals(result.getMessage(), "Invalid Permission Level");
    }

    @Test
    @DisplayName("Test validation of request message that has a permission level that is greater than 0")
    public void testUAttributeValidatorRequestValidPermissionLevel() {
        UMessage message = UMessageBuilder.request(buildDefaultUUri(), buildMethodUUri(), 1000)
        .withPermissionLevel(1).build();

        UAttributesValidator validator = UAttributesValidator.getValidator(message.getAttributes());
        ValidationResult result = validator.validate(message.getAttributes());
        assertTrue(result.isSuccess());
        assertFalse(validator.isExpired(message.getAttributes()));
        assertEquals(validator.toString(), "UAttributesValidator.Request");
    }

    @Test
    @DisplayName("Test validation of request message that has TTL that is less than 0")
    public void testUAttributeValidatorRequestInvalidTTL() {
        UMessage message = UMessageBuilder.publish(buildTopicUUri()).withTtl(-1).build();

        UAttributesValidator validator = UAttributesValidator.getValidator(message.getAttributes());
        ValidationResult result = validator.validate(message.getAttributes());
        assertTrue(result.isFailure());
        assertFalse(validator.isExpired(message.getAttributes()));
        assertEquals(validator.toString(), "UAttributesValidator.Publish");
        assertEquals(result.getMessage(), "Invalid TTL [-1]");
    }

    @Test
    @DisplayName("Test validation of request message where the message has expired")
    public void testUAttributeValidatorRequestExpired() throws InterruptedException {
        UMessage message = UMessageBuilder.request(buildDefaultUUri(), buildMethodUUri(), 1).build();
        Thread.sleep(100);
        UAttributesValidator validator = UAttributesValidator.getValidator(message.getAttributes());
        assertTrue(validator.isExpired(message.getAttributes()));
    }

    @Test
    @DisplayName("Test validator isExpired() for an ID that is mall formed and doesn't have the time")
    public void testUAttributeValidatorRequestExpiredMalformedId() {
        UAttributes attributes = UAttributes.newBuilder()
            .setId(UUID.getDefaultInstance())
            .setType(UMessageType.UMESSAGE_TYPE_REQUEST)
            .build();
        UAttributesValidator validator = UAttributesValidator.getValidator(attributes);
        assertFalse(validator.isExpired(attributes));
    }

    @Test
    @DisplayName("Test validation fails when a publish messages has a reqid")
    public void testUAttributeValidatorPublishWithReqId() {
        UMessage publish = UMessageBuilder.publish(buildTopicUUri()).build();
        UAttributes attributes = UAttributes.newBuilder()
            .mergeFrom(publish.getAttributes())
            .setReqid(UUID.getDefaultInstance())
            .build();

        UAttributesValidator validator = UAttributesValidator.getValidator(attributes);
        ValidationResult result = validator.validate(attributes);
        assertTrue(result.isFailure());
        assertEquals(validator.toString(), "UAttributesValidator.Publish");
        assertEquals(result.getMessage(), "Message should not have a reqid");
    }

    @Test
    @DisplayName("Test notification validation where the sink is missing")
    public void testUAttributeValidatorNotificationMissingSink() {
        final UMessage message = UMessageBuilder.notification(buildTopicUUri(), buildDefaultUUri()).build();
        final UAttributes attributes = UAttributes.newBuilder().mergeFrom(message.getAttributes()).clearSink().build();
        final UAttributesValidator validator = UAttributesValidator.getValidator(attributes);
        final ValidationResult result = validator.validate(attributes);

        assertTrue(result.isFailure());
        assertEquals(validator.toString(), "UAttributesValidator.Notification");
        assertEquals(result.getMessage(), "Missing Sink");
    }

    @Test
    @DisplayName("Test notification validation where the sink the default instance")
    public void testUAttributeValidatorNotificationDefaultSink() {
        final UMessage message = UMessageBuilder.notification(buildTopicUUri(), buildDefaultUUri()).build();
        final UAttributes attributes = UAttributes.newBuilder().mergeFrom(message.getAttributes()).setSink(UUri.getDefaultInstance()).build();
        final UAttributesValidator validator = UAttributesValidator.getValidator(attributes);
        final ValidationResult result = validator.validate(attributes);

        assertTrue(result.isFailure());
        assertEquals(validator.toString(), "UAttributesValidator.Notification");
        assertEquals(result.getMessage(), "Missing Sink");
    }

    @Test
    @DisplayName("Test notification validation where the sink is NOT the defaultResourceId")
    public void testUAttributeValidatorNotificationDefaultResourceId() {
        final UMessage message = UMessageBuilder.notification(buildTopicUUri(), buildTopicUUri()).build();
        final UAttributesValidator validator = UAttributesValidator.getValidator(message.getAttributes());
        final ValidationResult result = validator.validate(message.getAttributes());

        assertTrue(result.isFailure());
        assertEquals(validator.toString(), "UAttributesValidator.Notification");
        assertEquals(result.getMessage(), "Invalid Sink Uri");
    }

    @Test
    @DisplayName("Test validatePriority when priority is less than CS0")
    public void testUAttributeValidatorValidatePriorityLessThanCS0() {
        final UMessage message = UMessageBuilder.publish(buildTopicUUri()).build();
        final UAttributes attributes = UAttributes.newBuilder().mergeFrom(message.getAttributes()).setPriority(UPriority.UPRIORITY_UNSPECIFIED).build();
        final UAttributesValidator validator = UAttributesValidator.getValidator(attributes);
        final ValidationResult result = validator.validate(attributes);

        assertTrue(result.isFailure());
        assertEquals(validator.toString(), "UAttributesValidator.Publish");
        assertEquals(result.getMessage(), "Invalid UPriority [UPRIORITY_UNSPECIFIED]");
    }

    @Test
    @DisplayName("Test validatePriority when priority CS0")
    public void testUAttributeValidatorValidatePriorityIsCS0() {
        final UMessage message = UMessageBuilder.publish(buildTopicUUri()).build();
        final UAttributes attributes = UAttributes.newBuilder().
            mergeFrom(message.getAttributes()).setPriority(UPriority.UPRIORITY_CS0).build();
        final UAttributesValidator validator = UAttributesValidator.getValidator(attributes);
        final ValidationResult result = validator.validate(attributes);

        assertTrue(result.isFailure());
        assertEquals(validator.toString(), "UAttributesValidator.Publish");
        assertEquals(result.getMessage(), "Invalid UPriority [UPRIORITY_CS0]");
    }

    @Test
    @DisplayName("Test validateId when id is missing")
    public void testUAttributeValidatorValidateIdMissing() {
        final UMessage message = UMessageBuilder.publish(buildTopicUUri()).build();
        final UAttributes attributes = UAttributes.newBuilder().mergeFrom(message.getAttributes()).clearId().build();
        final UAttributesValidator validator = UAttributesValidator.getValidator(attributes);
        final ValidationResult result = validator.validate(attributes);

        assertTrue(result.isFailure());
        assertEquals(validator.toString(), "UAttributesValidator.Publish");
        assertEquals(result.getMessage(), "Missing id");
    }

    @Test
    @DisplayName("Test validateId when id is the default instance")
    public void testUAttributeValidatorValidateIdDefault() {
        final UMessage message = UMessageBuilder.publish(buildTopicUUri()).build();
        final UAttributes attributes = UAttributes.newBuilder().mergeFrom(message.getAttributes()).setId(UUID.getDefaultInstance()).build();
        final UAttributesValidator validator = UAttributesValidator.getValidator(attributes);
        final ValidationResult result = validator.validate(attributes);

        assertTrue(result.isFailure());
        assertEquals(validator.toString(), "UAttributesValidator.Publish");
        assertEquals(result.getMessage(), "Attributes must contain valid uProtocol UUID in id property");
    }

    @Test
    @DisplayName("Test publish validateSink when sink is not empty")
    public void testUAttributeValidatorValidateSinkNotEmpty() {
        final UMessage message = UMessageBuilder.publish(buildTopicUUri()).build();
        final UAttributes attributes = UAttributes.newBuilder().mergeFrom(message.getAttributes()).setSink(buildDefaultUUri()).build();
        final UAttributesValidator validator = UAttributesValidator.getValidator(attributes);
        final ValidationResult result = validator.validate(attributes);

        assertTrue(result.isFailure());
        assertEquals(validator.toString(), "UAttributesValidator.Publish");
        assertEquals(result.getMessage(), "Sink should not be present");
    }

    @Test
    @DisplayName("Test validateSink of a request message that is missing a sink")
    public void testUAttributeValidatorValidateSinkMissing() {
        final UMessage message = UMessageBuilder.request(buildDefaultUUri(), buildMethodUUri(), 1000).build();
        final UAttributes attributes = UAttributes.newBuilder().mergeFrom(message.getAttributes()).clearSink().build();
        final UAttributesValidator validator = UAttributesValidator.getValidator(attributes);
        final ValidationResult result = validator.validate(attributes);

        assertTrue(result.isFailure());
        assertEquals(validator.toString(), "UAttributesValidator.Request");
        assertEquals(result.getMessage(), "Missing Sink");
    }

    @Test
    @DisplayName("Test validateTtl of a request message where ttl is less than 0")
    public void testUAttributeValidatorValidateTtlLessThanZero() {
        final UMessage message = UMessageBuilder.request(buildDefaultUUri(), buildMethodUUri(), -1).build();
        final UAttributesValidator validator = UAttributesValidator.getValidator(message.getAttributes());
        final ValidationResult result = validator.validate(message.getAttributes());

        assertTrue(result.isFailure());
        assertEquals(validator.toString(), "UAttributesValidator.Request");
        assertEquals(result.getMessage(), "Invalid TTL [-1]");
    }

    @Test
    @DisplayName("Test validatePriority of a request message where priority is less than CS4")
    public void testUAttributeValidatorValidatePriorityLessThanCS4() {
        final UMessage message = UMessageBuilder.request(buildDefaultUUri(), buildMethodUUri(), 1000).build();
        final UAttributes attributes = UAttributes.newBuilder().mergeFrom(message.getAttributes()).setPriority(UPriority.UPRIORITY_CS3).build();
        final UAttributesValidator validator = UAttributesValidator.getValidator(attributes);
        final ValidationResult result = validator.validate(attributes);

        assertTrue(result.isFailure());
        assertEquals(validator.toString(), "UAttributesValidator.Request");
        assertEquals(result.getMessage(), "Invalid UPriority [UPRIORITY_CS3]");
    }

    @Test
    @DisplayName("Test validateSink for a response message where the sink is missing")
    public void testUAttributeValidatorValidateSinkResponseMissing() {
        final UMessage request = UMessageBuilder.request(buildDefaultUUri(), buildMethodUUri(), 1000).build();
        final UMessage response = UMessageBuilder.response(request.getAttributes()).build();
        final UAttributes attributes = UAttributes.newBuilder().mergeFrom(response.getAttributes()).clearSink().build();
        final UAttributesValidator validator = UAttributesValidator.getValidator(attributes);
        final ValidationResult result = validator.validate(attributes);

        assertTrue(result.isFailure());
        assertEquals(validator.toString(), "UAttributesValidator.Response");
        assertEquals(result.getMessage(), "Missing Sink");
    }

    @Test
    @DisplayName("Test validateSink for a response message where the sink is the default instance")
    public void testUAttributeValidatorValidateSinkResponseDefault() {
        final UMessage request = UMessageBuilder.request(buildDefaultUUri(), buildMethodUUri(), 1000).build();
        final UMessage response = UMessageBuilder.response(request.getAttributes()).build();
        final UAttributes attributes = UAttributes.newBuilder().mergeFrom(response.getAttributes()).setSink(UUri.getDefaultInstance()).build();
        final UAttributesValidator validator = UAttributesValidator.getValidator(attributes);
        final ValidationResult result = validator.validate(attributes);

        assertTrue(result.isFailure());
        assertEquals(validator.toString(), "UAttributesValidator.Response");
        assertEquals(result.getMessage(), "Missing Sink");
    }

    @Test
    @DisplayName("Test validateSink for a response message where the sink is NOT the defaultResourceId")
    public void testUAttributeValidatorValidateSinkResponseDefaultResourceId() {
        final UMessage request = UMessageBuilder.request(buildMethodUUri(), buildDefaultUUri(), 1000).build();
        final UMessage response = UMessageBuilder.response(request.getAttributes()).build();
        final UAttributesValidator validator = UAttributesValidator.getValidator(response.getAttributes());
        final ValidationResult result = validator.validate(response.getAttributes());

        assertTrue(result.isFailure());
        assertEquals(validator.toString(), "UAttributesValidator.Response");
        assertEquals(result.getMessage(), "Invalid Sink Uri");
    }

    @Test
    @DisplayName("Test validateReqId for a response message when the reqid is missing")
    public void testUAttributeValidatorValidateReqIdMissing() {
        final UMessage request = UMessageBuilder.request(buildDefaultUUri(), buildMethodUUri(), 1000).build();
        final UMessage response = UMessageBuilder.response(request.getAttributes()).build();
        final UAttributes attributes = UAttributes.newBuilder().mergeFrom(response.getAttributes()).clearReqid().build();
        final UAttributesValidator validator = UAttributesValidator.getValidator(attributes);
        final ValidationResult result = validator.validate(attributes);

        assertTrue(result.isFailure());
        assertEquals(validator.toString(), "UAttributesValidator.Response");
        assertEquals(result.getMessage(), "Missing correlationId");
    }

    @Test
    @DisplayName("Test validateReqId for a response message when the reqid is the default instance")
    public void testUAttributeValidatorValidateReqIdDefault() {
        final UMessage request = UMessageBuilder.request(buildDefaultUUri(), buildMethodUUri(), 1000).build();
        final UMessage response = UMessageBuilder.response(request.getAttributes()).build();
        final UAttributes attributes = UAttributes.newBuilder().mergeFrom(response.getAttributes()).setReqid(UUID.getDefaultInstance()).build();
        final UAttributesValidator validator = UAttributesValidator.getValidator(attributes);
        final ValidationResult result = validator.validate(attributes);

        assertTrue(result.isFailure());
        assertEquals(validator.toString(), "UAttributesValidator.Response");
        assertEquals(result.getMessage(), "Missing correlationId");
    }

    @Test
    @DisplayName("Test validateReqId for a response message when the reqid not a valid uprotocol UUID")
    public void testUAttributeValidatorValidateReqIdInvalid() {
        final UMessage request = UMessageBuilder.request(buildDefaultUUri(), buildMethodUUri(), 1000).build();
        final UMessage response = UMessageBuilder.response(request.getAttributes()).build();
        final UAttributes attributes = UAttributes.newBuilder().mergeFrom(response.getAttributes()).setReqid(UUID.newBuilder().setLsb(0xbeadbeef).setMsb(0xdeadbeef)).build();
        final UAttributesValidator validator = UAttributesValidator.getValidator(attributes);
        final ValidationResult result = validator.validate(attributes);

        assertTrue(result.isFailure());
        assertEquals(validator.toString(), "UAttributesValidator.Response");
        assertEquals(result.getMessage(), "Invalid correlation UUID");
    }
}
