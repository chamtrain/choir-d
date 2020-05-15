/*
 * Copyright 2019 The Board of Trustees of The Leland Stanford Junior University.
 * All Rights Reserved.
 *
 * See the NOTICE and LICENSE files distributed with this work for information
 * regarding copyright ownership and licensing. You may not use this file except
 * in compliance with a written license agreement with Stanford University.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See your
 * License for the specific language governing permissions and limitations under
 * the License.
 */
(function() {
  'use strict';

  angular.module('OpioidResponsesReport', ['ChoirApi'])
          .controller('OpioidResponsesReportCtrl', ['$filter','choirApi', OpioidResponsesReportCtrl]);


  function OpioidResponsesReportCtrl($filter, choirApi) {
    var vm = this;

    // report parameters
    vm.fromDt = null;
    vm.toDt = null;

    vm.clbppatients = {};
    vm.clbppatients.title = '';
    vm.clbppatients.value  = '';

    vm.allpatients = {};
    vm.allpatients.title = '';
    vm.allpatients.value = '';
    vm.allpatients.valueList = [];

    vm.parameters = null;
    vm.form = null;
    vm.arrayLines = null;

    // methods
    vm.showParameters = showParameters;
    vm.runReport = runReport;
    vm.downloadReport = downloadReport;

    // Initialization
    showParameters();

    /*
     * Show the report options
     */
    function showParameters() {
      vm.fromDt = null;
      vm.toDt = null;

      vm.clbppatients.title = '';
      vm.clbppatients.value  = '';

      vm.allpatients = {};
      vm.allpatients.title = '';
      vm.allpatients.value = '';
      vm.allpatients.valueList = [];
      vm.result = null;
      vm.parameters = null;
      vm.form = null;
      vm.arrayLines = null;

      choirApi.getReport("opioidResponses", null, function (result) {
        vm.result = result;
        vm.parameters = result.reportParameters;

        for (var parameter in vm.parameters ) {
          if (vm.parameters[parameter].name === 'fromDt') {
            vm.fromDt = vm.parameters[parameter].value;
          }
          if (vm.parameters[parameter].name === 'toDt') {
            vm.toDt= vm.parameters[parameter].value;
          }
          if (vm.parameters[parameter].name === 'allpatients') {
            vm.allpatients.title = vm.parameters[parameter].title;
            for (var val in vm.parameters[parameter].VALUELIST) {
              vm.allpatients.valueList.push(vm.parameters[parameter].VALUELIST[val]);
            }
            vm.allpatients.value = 'All patients'; // default to all
          }
          if (vm.parameters[parameter].name === 'clbppatients') {
            vm.clbppatients.title = vm.parameters[parameter].title;
            vm.clbppatients.value = vm.parameters[parameter].value;
          }
        }
      });
    }

    function runReport() {
      window.parent.showLoadingPopup();
      /* Build the parameters. Reformat the dates from the displayed MM/dd/yyyy
         to yyyy-MM-dd for the server code */
      var jsonData = {};
      var fromParts = vm.fromDt.split("/");
      jsonData["fromDt"] = fromParts[2] + "-" + fromParts[0] + "-" + fromParts[1];
      var toParts = vm.toDt.split("/");
      jsonData["toDt"] = toParts[2] + "-" + toParts[0] + "-" + toParts[1];
      jsonData["allpatients"] = vm.allpatients.value;
      jsonData["clbppatients"] = vm.clbppatients.value;

      choirApi.getReport("opioidResponses", JSON.stringify(jsonData), function (result) {
        vm.result = result;
        vm.arrayLines = result.reportDataSet;
        window.parent.hideLoadingPopup();
      });
    }

    function downloadReport() {
      choirApi.downloadReport(vm.result, "OpioidResponsesReport");
    }
  }
})();
