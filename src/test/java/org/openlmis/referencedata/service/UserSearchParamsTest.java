/*
 * This program is part of the OpenLMIS logistics management information system platform software.
 * Copyright © 2017 VillageReach
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU Affero General Public License for more details. You should have received a copy of
 * the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses.  For additional information contact info@OpenLMIS.org. 
 */

package org.openlmis.referencedata.service;

import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Sets;
import org.junit.Test;
import org.openlmis.referencedata.exception.ValidationMessageException;
import java.util.Collections;
import java.util.UUID;

public class UserSearchParamsTest {

  public static final String TEST = "test";

  @Test
  public void shouldReturnTrueIfAllFieldsAreEmpty() {
    UserSearchParams userSearchParams = new UserSearchParams();

    assertTrue(userSearchParams.isEmpty());
  }

  @Test
  public void shouldReturnFalseIfAnyFieldIsEmpty() {
    UserSearchParams userSearchParams = new UserSearchParams();
    userSearchParams.id = Collections.singleton(TEST);
    assertFalse(userSearchParams.isEmpty());

    userSearchParams = new UserSearchParams();
    userSearchParams.username = TEST;
    assertFalse(userSearchParams.isEmpty());

    userSearchParams = new UserSearchParams();
    userSearchParams.firstName = TEST;
    assertFalse(userSearchParams.isEmpty());

    userSearchParams = new UserSearchParams();
    userSearchParams.lastName = TEST;
    assertFalse(userSearchParams.isEmpty());

    userSearchParams = new UserSearchParams();
    userSearchParams.email = TEST;
    assertFalse(userSearchParams.isEmpty());

    userSearchParams = new UserSearchParams();
    userSearchParams.homeFacilityId = TEST;
    assertFalse(userSearchParams.isEmpty());

    userSearchParams = new UserSearchParams();
    userSearchParams.verified = true;
    assertFalse(userSearchParams.isEmpty());

    userSearchParams = new UserSearchParams();
    userSearchParams.active = true;
    assertFalse(userSearchParams.isEmpty());

    userSearchParams = new UserSearchParams();
    userSearchParams.loginRestricted = true;
    assertFalse(userSearchParams.isEmpty());

    userSearchParams = new UserSearchParams();
    userSearchParams.extraData = Collections.singletonMap(TEST, TEST);
    assertFalse(userSearchParams.isEmpty());
  }

  @Test
  public void shouldReturnListOfIdsParsedToUuid() {
    UUID idOne = UUID.randomUUID();
    UUID idTwo = UUID.randomUUID();
    UserSearchParams userSearchParams =
        new UserSearchParams(
            Sets.newHashSet(idOne.toString(), idTwo.toString()));

    assertThat(userSearchParams.getIds(), hasItems(idOne, idTwo));
  }

  @Test
  public void shouldReturnHomeFacilityUuid() {
    UUID id = UUID.randomUUID();
    UserSearchParams userSearchParams = new UserSearchParams();
    userSearchParams.setHomeFacilityId(id.toString());

    assertEquals(id, userSearchParams.getHomeFacilityUuid());
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowExceptionIdHomeFacilityUuidIsNotValid() {
    UserSearchParams userSearchParams = new UserSearchParams();
    userSearchParams.setHomeFacilityId("123");

    userSearchParams.getHomeFacilityUuid();
  }

}