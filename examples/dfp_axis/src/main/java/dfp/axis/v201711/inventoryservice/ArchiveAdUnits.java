// Copyright 2015 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package dfp.axis.v201711.inventoryservice;

import com.beust.jcommander.Parameter;
import com.google.api.ads.common.lib.auth.OfflineCredentials;
import com.google.api.ads.common.lib.auth.OfflineCredentials.Api;
import com.google.api.ads.common.lib.utils.examples.CodeSampleParams;
import com.google.api.ads.dfp.axis.factory.DfpServices;
import com.google.api.ads.dfp.axis.utils.v201711.StatementBuilder;
import com.google.api.ads.dfp.axis.v201711.AdUnit;
import com.google.api.ads.dfp.axis.v201711.AdUnitPage;
import com.google.api.ads.dfp.axis.v201711.InventoryServiceInterface;
import com.google.api.ads.dfp.axis.v201711.UpdateResult;
import com.google.api.ads.dfp.lib.client.DfpSession;
import com.google.api.ads.dfp.lib.utils.examples.ArgumentNames;
import com.google.api.client.auth.oauth2.Credential;

/**
 * This example archives ad units underneath a parent ad unit, as well as the
 * parent ad unit. To create ad units, run CreateAdUnits.java. To determine
 * which ad units exist, run GetAllAdUnits.java.
 *
 * Credentials and properties in {@code fromFile()} are pulled from the
 * "ads.properties" file. See README for more info.
 */
public class ArchiveAdUnits {

  private static class ArchiveAdUnitsParams extends CodeSampleParams {
    @Parameter(names = ArgumentNames.PARENT_AD_UNIT_ID, required = true,
        description = "The ad unit ID to archive underneath.")
    private String parentAdUnitId;
  }

  public static void runExample(DfpServices dfpServices, DfpSession session, String parentAdUnitId)
      throws Exception {
    // Get the InventoryService.
    InventoryServiceInterface inventoryService =
        dfpServices.get(session, InventoryServiceInterface.class);

    // Create a statement to select ad units under the parent ad unit and the
    // parent ad unit.
    StatementBuilder statementBuilder = new StatementBuilder()
        .where("parentId = :parentId or id = :parentId")
        .orderBy("id ASC")
        .limit(StatementBuilder.SUGGESTED_PAGE_LIMIT)
        .withBindVariableValue("parentId", parentAdUnitId);

    // Default for total result set size.
    int totalResultSetSize = 0;

    do {
      // Get ad units by statement.
      AdUnitPage page = inventoryService.getAdUnitsByStatement(statementBuilder.toStatement());

      if (page.getResults() != null) {
        totalResultSetSize = page.getTotalResultSetSize();
        int i = page.getStartIndex();
        for (AdUnit adUnit : page.getResults()) {
          System.out.printf(
              "%d) Ad unit with ID '%s' and name '%s' will be archived.%n", i++,
              adUnit.getId(), adUnit.getName());
        }
      }

      statementBuilder.increaseOffsetBy(StatementBuilder.SUGGESTED_PAGE_LIMIT);
    } while (statementBuilder.getOffset() < totalResultSetSize);

    System.out.printf("Number of ad units to be archived: %d%n", totalResultSetSize);

    if (totalResultSetSize > 0) {
      // Remove limit and offset from statement.
      statementBuilder.removeLimitAndOffset();

      // Create action.
      com.google.api.ads.dfp.axis.v201711.ArchiveAdUnits action =
          new com.google.api.ads.dfp.axis.v201711.ArchiveAdUnits();

      // Perform action.
      UpdateResult result =
          inventoryService.performAdUnitAction(action, statementBuilder.toStatement());

      if (result != null && result.getNumChanges() > 0) {
        System.out.printf("Number of ad units archived: %d%n", result.getNumChanges());
      } else {
        System.out.println("No ad units were archived.");
      }
    }
  }

  public static void main(String[] args) throws Exception {
    // Generate a refreshable OAuth2 credential.
    Credential oAuth2Credential = new OfflineCredentials.Builder()
        .forApi(Api.DFP)
        .fromFile()
        .build()
        .generateCredential();

    // Construct a DfpSession.
    DfpSession session = new DfpSession.Builder()
        .fromFile()
        .withOAuth2Credential(oAuth2Credential)
        .build();

    DfpServices dfpServices = new DfpServices();

    ArchiveAdUnitsParams params = new ArchiveAdUnitsParams();
    if (!params.parseArguments(args)) {
      // Either pass the required parameters for this example on the command line, or insert them
      // into the code here. See the parameter class definition above for descriptions.
      params.parentAdUnitId = "INSERT_PARENT_AD_UNIT_ID_HERE";
    }

    runExample(dfpServices, session, params.parentAdUnitId);
  }
}
