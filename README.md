DTN Android Application
=====================================
"DTN App" serves the purpose of sending messages and data using a phone's in-built technologies like Bluetooth.

# Get the Application Working

## Changing the device names
Change device names in Constants.java. Format of device name: `DTN-<Device's Serial Number>`.

            public interface DeviceNames {
                    String originDevice = "DTN-PLEGAR1762212642";
                    String secondRouterDevice = "DTN-1641b121";
                    String thirdRouterDevice = "DTN-51a33087";
                    String destinationDevice = "DTN-5da9d6090804";

                }

UUIDManager.java uses these device names to set the UUID of the device.

## Set the UUIDs
            public interface UUIDs {
                        UUID mmSocket_UUID = UUID.fromString("6e7bd336-5676-407e-a41c-0691e1964345"); // UUID is uniquely generated
                        UUID ACK_UUID = UUID.fromString("b03901e4-710c-4509-9718-a3d15882d050");
                        UUID BW_UUID = UUID.fromString("aa401ee7-3bb2-410c-9dda-2128726513a1");

                        UUID destination_MMSocket_UUID = UUID.fromString("fa249bcd-e53c-4965-a9f9-d7ea5d6f0040");
                        UUID destination_ACK_UUID = UUID.fromString("d9c13848-d7be-48a1-ac11-5f0c082791c7");
                        UUID destination_BW_UUID = UUID.fromString("5c6ae5f9-cb04-4a71-9552-ffe426b02b99");
                    }

You can change the UUIDs in Constants.java.

UUIDs can be generated [here](https://www.uuidgenerator.net/).

### Explanation
#### Setting the UUIDs to send and receive data
For our demonstration we used 4 different phones. Before flashing the application to the first phone which had a device name `DTN-PLEGAR1762212642` we ensured the UUIDs in Constants.java are correct. The other two devices acted as `Routers`. The `Routers` forwarded the data to the `destination device`.

We set the value of `destination_MMSocket_UUID`, `destination_BW_UUID` matching the second's phone `mmSocket_UUID`, `BW_UUID`. The same process should be used before flashing the application to the second phone. And again, we set the value of `destination_MMSocket_UUID`, `destination_BW_UUID` matching the third's phone `mmSocket_UUID`, `BW_UUID`.

This process continues until you are done flashing the 4th phone.

#### Setting the UUIDs to send and receive ACK messages
The fourth phone's `destination_MMSocket_UUID`, `destination_ACK_UUID`, `destination_BW_UUID` should be matching the third's phone `mmSocket_UUID`, `ACK_UUID`, `BW_UUID` in order to send the ACK message.

`destination_ACK_UUID` of the third phone should match that of the `ACK_UUID` of the second phone. The second phone's `destination_ACK_UUID` should match that of the `ACK_UUID` of the first phone.

The first phone's UUID can be any UUID because the first phone does not send any ACK message.

The ACK message delivery works in a **reverse process**.