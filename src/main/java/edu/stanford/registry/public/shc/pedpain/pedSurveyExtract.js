/*
* Copyright 2020 The Board of Trustees of The Leland Stanford Junior University.
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

  angular.module('PedSurveyDataExtract', ['ChoirApi'])
          .controller('PedSurveyDataExtractCtrl', ['$filter','choirApi', PedSurveyDataExtractCtrl]);
  function PedSurveyDataExtractCtrl($filter, choirApi) {
    let vm = this;

    vm.fromDt = null;
    vm.toDt = null;

    vm.form = null;
    vm.arrayLines = null;
    vm.fieldList = [];
    vm.selectedFieldList = [];

    // methods
    vm.showParameters = showParameters;
    vm.runExtract = runExtract;
    vm.downloadExtract = downloadExtract;
    vm.checkAll = checkAll;
    vm.uncheckAll = uncheckAll;
    vm.isChecked = isChecked;
    vm.checked = false;
    vm.toggleSelection = toggleSelection;

    showParameters();

    /*
     * Show the Extract options
     */
    function showParameters() {
      let today = new Date();
      let lastyr = new Date (today.getTime() - ((31557600 - 86400) * 1000));

      vm.fromDt = (parseInt(lastyr.getMonth(),10) +1)+ "/" + lastyr.getDate() + "/" + lastyr.getFullYear();
      vm.toDt = (parseInt(today.getMonth(), 10) +1) + "/" + today.getDate() + "/" + today.getFullYear();

      vm.result = null;
      vm.selectedFieldList = [];
      vm.form = null;
      vm.arrayLines = null;

      choirApi.getExtract("rpt_pedpain_surveys", null, function (result) {
        vm.result = result;
        vm.fieldList = [];
        for (let i = 0; i < result.fieldList.length; i++) {
          let field = {};
          field.name = result.fieldList[i];
          field.selected = true;
          field.id = i;
          vm.fieldList.push(field);
          vm.selectedFieldList.push(field);
        }
        vm.checked = true;
      });
    }

    function runExtract() {
      if (vm.selectedFieldList.length < 1) {
        return;
      }
      window.parent.showLoadingPopup();
      /* Build the parameters. Reformat the dates from the displayed MM/dd/yyyy
         to yyyy-MM-dd for the server code */
      let jsonParams = {};
      let fromDtParts = vm.fromDt.split("/");
      let toDtParts = vm.toDt.split("/");
      jsonParams["fromDt"] = fromDtParts[2] + "-" + fromDtParts[0] + "-" + fromDtParts[1];
      jsonParams["toDt"] = toDtParts[2] + "-" + toDtParts[0] + "-" + toDtParts[1];

      let extractfieldList = [];
      for  (let i = 0; i < vm.selectedFieldList.length; i++) {
        extractfieldList.push(vm.selectedFieldList[i].name);
      }
      jsonParams["fields"] = extractfieldList;
      choirApi.getExtract("rpt_pedpain_surveys", JSON.stringify(jsonParams), function (result) {
        vm.result = result;
        vm.arrayLines = result.EXTRACTDATA;
        window.parent.hideLoadingPopup();
      });
    }

    function downloadExtract() {
      let jsonData = {};
      jsonData["reportDataSet"] = vm.arrayLines;
      choirApi.downloadReport(jsonData, "PediatricPainCHOIRExtract");
    }

    function toggleSelection(item) {
      item.selected = !item.selected;
      let idx = vm.selectedFieldList.indexOf(item);
      // Is currently selected
      if (idx > -1) {
        vm.selectedFieldList.splice(idx, 1);
      }
      // Is newly selected
      else {
        vm.selectedFieldList.push(item);
      }
      vm.checked = vm.selectedFieldList.length > 0;
    }

    function checkAll () {
      for (let i = vm.fieldList.length; i > 0 ; i--) {
        vm.selectedFieldList.splice(i-1, 1);
      }
      for (let i = 0; i < vm.fieldList.length; i++) {
        vm.selectedFieldList.push(vm.fieldList[i]);
        vm.selectedFieldList[i].selected = true;
      }
      vm.checked = true;
      return true;
    }

    function uncheckAll() {
      for (let i = vm.fieldList.length; i > 0 ; i--) {
        vm.fieldList[i-1].selected = false;
        vm.selectedFieldList.splice(i-1, 1);

      }
      vm.checked = false;
      return true;
    }

    function isChecked(val)  {
      let idx = vm.selectedFieldList.indexOf(val);
      // Is currently selected
      if (idx > -1) {
        return true;
      }
      for (let i = 0; i < vm.selectedFieldList.length; i++) {
        if (val === vm.selectedFieldList[i]) {
          return true;
        }
      }
      return false;
    }
  }
})();
