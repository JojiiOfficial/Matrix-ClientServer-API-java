package de.jojii.matrixclientserver.Bot.Events;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class RoomEventTest {

    @Test
    void parseAllEvents() {
        final JSONObject json = new JSONObject("{\"next_batch\": \"s9996_43873_289_1246_1182_1_513_605_1\",\"org.matrix.msc2732.device_unused_fallback_key_types\": [],\"device_lists\": {\"changed\": [\"@bot1:example.org\"]},\"presence\": {\"events\": [{\"sender\": \"@me:example.org\",\"type\": \"m.presence\",\"content\": {\"currently_active\": true,\"last_active_ago\": 11213,\"presence\": \"online\"}},{\"sender\": \"@bot1:example.org\",\"type\": \"m.presence\",\"content\": {\"currently_active\": true,\"last_active_ago\": 23,\"presence\": \"online\"}}]},\"device_one_time_keys_count\": {\"signed_curve25519\": 0}}");

        final List<RoomEvent> result = RoomEvent.parseAllEvents(json);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void parseAllEvents_typing() {
        final JSONObject json = new JSONObject("{\"rooms\":{\"join\":{\"!qdHiCcRkEMxEAOXdAz:example.org\":{\"summary\":{},\"timeline\":{\"limited\":false,\"events\":[],\"prev_batch\":\"s9997_43970_293_1247_1185_1_514_609_1\"},\"ephemeral\":{\"events\":[{\"type\":\"m.typing\",\"content\":{\"user_ids\":[\"@me:example.org\"]}}]},\"account_data\":{\"events\":[]},\"state\":{\"events\":[]},\"org.matrix.msc2654.unread_count\":2,\"unread_notifications\":{\"highlight_count\":0,\"notification_count\":2}}}},\"next_batch\":\"s9997_43970_294_1247_1185_1_514_609_1\",\"org.matrix.msc2732.device_unused_fallback_key_types\":[],\"device_one_time_keys_count\":{\"signed_curve25519\":0}}");
        final JSONObject expectedContent = new JSONObject("{\"user_ids\":[\"@me:example.org\"]}");

        final List<RoomEvent> result = RoomEvent.parseAllEvents(json);

        assertNotNull(result);
        final RoomEvent event = result.get(0);

        assertEquals("m.typing", event.getType());
        assertEquals("!qdHiCcRkEMxEAOXdAz:example.org", event.getRoom_id());
        assertEquals(expectedContent.toString(), event.getContent().toString());
    }

    @Test
    void parseAllEvents_invite() {
        final JSONObject json = new JSONObject("{\"rooms\":{\"invite\":{\"!mXzFBiCxtFEUwtumFb:example.org\":{\"invite_state\":{\"events\":[{\"state_key\":\"\",\"sender\":\"@me:example.org\",\"type\":\"m.room.create\",\"content\":{\"room_version\":\"6\",\"creator\":\"@me:example.org\"}},{\"state_key\":\"\",\"sender\":\"@me:example.org\",\"type\":\"m.room.join_rules\",\"content\":{\"join_rule\":\"invite\"}},{\"state_key\":\"\",\"sender\":\"@me:example.org\",\"type\":\"m.room.name\",\"content\":{\"name\":\"botRoom2\"}},{\"state_key\":\"@me:example.org\",\"sender\":\"@me:example.org\",\"type\":\"m.room.member\",\"content\":{\"avatar_url\":\"mxc://example.org/JYguYRaQMoSNsseshNTykYmL\",\"displayname\":\"Timmi Tester\",\"membership\":\"join\"}},{\"state_key\":\"@bot1:example.org\",\"origin_server_ts\":1642797670911,\"event_id\":\"$t90oH1VsMcnWU6r8zv2zUYmNXgsvdTJFLoAtUOZerLg\",\"sender\":\"@me:example.org\",\"unsigned\":{\"age\":92},\"type\":\"m.room.member\",\"content\":{\"displayname\":\"bot1\",\"membership\":\"invite\"}}]}}}},\"next_batch\":\"s10006_44006_295_1247_1188_1_514_614_1\",\"org.matrix.msc2732.device_unused_fallback_key_types\":[],\"device_one_time_keys_count\":{\"signed_curve25519\":0}}");

        final List<RoomEvent> result = RoomEvent.parseAllEvents(json);

        assertNotNull(result);
        assertEquals(5, result.size());

        assertEquals("m.room.create", result.get(0).getType());
        assertEquals("@me:example.org", result.get(0).getSender());
        assertEquals("!mXzFBiCxtFEUwtumFb:example.org", result.get(0).getRoom_id());

        assertEquals("m.room.join_rules", result.get(1).getType());
        assertEquals("@me:example.org", result.get(1).getSender());
        assertEquals("!mXzFBiCxtFEUwtumFb:example.org", result.get(1).getRoom_id());

        assertEquals("m.room.name", result.get(2).getType());
        assertEquals("@me:example.org", result.get(2).getSender());
        assertEquals("{\"name\":\"botRoom2\"}", result.get(2).getContent().toString());

        assertEquals("m.room.member", result.get(3).getType());
        assertEquals("@me:example.org", result.get(3).getSender());

        assertEquals("m.room.member", result.get(4).getType());
        assertEquals("@me:example.org", result.get(4).getSender());
        assertEquals("$t90oH1VsMcnWU6r8zv2zUYmNXgsvdTJFLoAtUOZerLg", result.get(4).getEvent_id());
        assertEquals("!mXzFBiCxtFEUwtumFb:example.org", result.get(4).getRoom_id());


    }

    @Test
    void parseAllEvents_join() {
        final JSONObject json = new JSONObject("{\"rooms\":{\"join\":{\"!mXzFBiCxtFEUwtumFb:example.org\":{\"summary\":{},\"timeline\":{\"limited\":true,\"events\":[{\"event_id\":\"$eUhkaMJ8eFiB6KJ0wnT6ptiODtGg8f_6NHgaAzdiztE\",\"sender\":\"@me:example.org\",\"type\":\"m.room.create\",\"content\":{\"room_version\":\"6\",\"creator\":\"@me:example.org\"}},{\"event_id\":\"$0XppN_8XoCSXPZgUNrOfGbLbkiNejjaHx7C8Err7pco\",\"sender\":\"@me:example.org\",\"type\":\"m.room.member\",\"content\":{\"avatar_url\":\"mxc://example.org/JYguYRaQMoSNsseshNTykYmL\",\"displayname\":\"Timmi Tester\",\"membership\":\"join\"}},{\"event_id\":\"$jYifgmMED_je9a7_53UenAVrVYYrCz8LveXDEXYdeZ4\",\"sender\":\"@me:example.org\",\"type\":\"m.room.power_levels\",\"content\":{\"events_default\":0,\"kick\":50,\"users_default\":0,\"redact\":50,\"historical\":100,\"invite\":0,\"users\":{\"@me:example.org\":100},\"events\":{\"m.room.name\":50,\"m.room.canonical_alias\":50,\"m.room.server_acl\":100,\"m.room.tombstone\":100,\"m.room.avatar\":50,\"m.room.power_levels\":100,\"m.room.history_visibility\":100,\"m.room.encryption\":100},\"state_default\":50,\"ban\":50}},{\"event_id\":\"$3QZ3TcKhMmg0AuuRGmaBPkQmU8_jsYNJMapwU6nSZ4c\",\"sender\":\"@me:example.org\",\"type\":\"m.room.join_rules\",\"content\":{\"join_rule\":\"invite\"}},{\"event_id\":\"$-A3kF5bgLzeIhKU7Jwo-Keuue9UDEfBpEEkRmJ0DkRs\",\"sender\":\"@me:example.org\",\"type\":\"m.room.history_visibility\",\"content\":{\"history_visibility\":\"shared\"}},{\"event_id\":\"$sAVjZ6pj5HuO14fLNWyG6HvePfWDjh6ZpTWyNYLwAUI\",\"sender\":\"@me:example.org\",\"type\":\"m.room.guest_access\",\"content\":{\"guest_access\":\"can_join\"}},{\"event_id\":\"$nHiyRZAk-BzfYgGRTpFqCnhYTFBYRAg8npZbxq3_g6A\",\"sender\":\"@me:example.org\",\"type\":\"m.room.name\",\"content\":{\"name\":\"botRoom2\"}},{\"event_id\":\"$t90oH1VsMcnWU6r8zv2zUYmNXgsvdTJFLoAtUOZerLg\",\"sender\":\"@me:example.org\",\"type\":\"m.room.member\",\"content\":{\"displayname\":\"bot1\",\"membership\":\"invite\"}},{\"event_id\":\"$PgmUhwmRv0WE8XQCLIFOB_8c1KWqNJzilZOJtpth_gI\",\"sender\":\"@bot1:example.org\",\"type\":\"m.room.member\",\"content\":{\"displayname\":\"bot1\",\"membership\":\"join\"}},{\"event_id\":\"$PgmUhwmRv0WE8XQCLIFOB_8c1KWqNJzilZOJtpth_gI\",\"sender\":\"@bot1:example.org\",\"type\":\"m.room.member\",\"content\":{\"displayname\":\"bot1\",\"membership\":\"join\"}}],\"prev_batch\":\"s10007_44006_295_1247_1188_1_514_614_1\"},\"ephemeral\":{\"events\":[]},\"account_data\":{\"events\":[]},\"state\":{\"events\":[]},\"org.matrix.msc2654.unread_count\":0,\"unread_notifications\":{\"highlight_count\":0,\"notification_count\":0}}}},\"next_batch\":\"s10007_44006_295_1247_1188_1_514_614_1\",\"org.matrix.msc2732.device_unused_fallback_key_types\":[],\"device_lists\":{\"changed\":[\"@me:example.org\",\"@bot1:example.org\"]},\"presence\":{\"events\":[{\"sender\":\"@me:example.org\",\"type\":\"m.presence\",\"content\":{\"currently_active\":true,\"last_active_ago\":10,\"presence\":\"online\"}}]},\"device_one_time_keys_count\":{\"signed_curve25519\":0}}");

        final List<String> expectedEventTypes = Arrays.asList("m.room.create", "m.room.member", "m.room.power_levels",
                "m.room.join_rules", "m.room.history_visibility", "m.room.guest_access", "m.room.name", "m.room.member",
                "m.room.member", "m.room.member");

        final List<RoomEvent> result = RoomEvent.parseAllEvents(json);

        assertNotNull(result);
        assertEquals(10, result.size());

        final List<String> eventTypes = result.stream().map(RoomEvent::getType).collect(Collectors.toList());
        assertEquals(expectedEventTypes, eventTypes);
    }

    @Test
    void parseAllEvents_file() {
        final JSONObject json = new JSONObject("{\"rooms\":{\"join\":{\"!mXzFBiCxtFEUwtumFb:example.org\":{\"summary\":{},\"timeline\":{\"limited\":false,\"events\":[{\"event_id\":\"$-e30UNeC64KCaZCSNMzAeJMzYVAncWIgUxAZ_tXl5Zc\",\"sender\":\"@me:example.org\",\"type\":\"m.room.message\",\"content\":{\"body\":\"red.png\",\"msgtype\":\"m.image\",\"url\":\"mxc://example.org/dQRxjroKFYddMfTrpUYrXMRP\",\"info\":{\"xyz.amorgan.blurhash\":\"U#Q[S_xa%~%2r?jZozj[.mkCR5j[%Mj[ayj[\",\"size\":98,\"w\":20,\"h\":20,\"mimetype\":\"image/png\"}}}],\"prev_batch\":\"s10040_44616_313_1252_1209_1_514_625_1\"},\"ephemeral\":{\"events\":[]},\"account_data\":{\"events\":[]},\"state\":{\"events\":[]},\"org.matrix.msc2654.unread_count\":8,\"unread_notifications\":{\"highlight_count\":0,\"notification_count\":8}}}},\"next_batch\":\"s10041_44616_313_1252_1209_1_514_625_1\",\"org.matrix.msc2732.device_unused_fallback_key_types\":[],\"device_one_time_keys_count\":{\"signed_curve25519\":0}}");

        final List<RoomEvent> result = RoomEvent.parseAllEvents(json);

        assertNotNull(result);
        assertEquals(1, result.size());

        assertEquals("m.room.message", result.get(0).getType());
        assertEquals("@me:example.org", result.get(0).getSender());
        assertEquals("!mXzFBiCxtFEUwtumFb:example.org", result.get(0).getRoom_id());
    }

    @Test
    void parseAllEvents_message() {
        final JSONObject json = new JSONObject("{\"rooms\":{\"join\":{\"!mXzFBiCxtFEUwtumFb:example.org\":{\"summary\":{},\"timeline\":{\"limited\":false,\"events\":[{\"event_id\":\"$ZkWVbGcKhKRwksx65QzHMPQ9p3XG4QP4dDho_5nz-F8\",\"sender\":\"@me:example.org\",\"type\":\"m.room.message\",\"content\":{\"body\":\"Hello, world!\",\"msgtype\":\"m.text\"}}],\"prev_batch\":\"s10041_44624_315_1252_1210_1_514_625_1\"},\"ephemeral\":{\"events\":[]},\"account_data\":{\"events\":[]},\"state\":{\"events\":[]},\"org.matrix.msc2654.unread_count\":9,\"unread_notifications\":{\"highlight_count\":0,\"notification_count\":9}}}},\"next_batch\":\"s10042_44624_315_1252_1210_1_514_625_1\",\"org.matrix.msc2732.device_unused_fallback_key_types\":[],\"device_one_time_keys_count\":{\"signed_curve25519\":0}}");

        final List<RoomEvent> result = RoomEvent.parseAllEvents(json);

        assertNotNull(result);
        assertEquals(1, result.size());

        assertEquals("m.room.message", result.get(0).getType());
        assertEquals("@me:example.org", result.get(0).getSender());
        assertEquals("!mXzFBiCxtFEUwtumFb:example.org", result.get(0).getRoom_id());
    }
}