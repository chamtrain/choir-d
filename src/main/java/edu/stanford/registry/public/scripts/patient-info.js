(function() {
  'use strict';

  angular.module('PatientInfo', ['PatientLookup','ChoirApi'])
          .controller('PatientInfoCtrl', ['$filter', 'choirApi', PatientInfoCtrl]);

  function PatientInfoCtrl($filter, choirApi) {
    var vm = this;

    // Get the current site
    var siteId = null;
    if (window.parent.getSiteId != null) {
      siteId = window.parent.getSiteId();
    }
    // Defaults for testing in a stand alone window
    if (siteId == null) {
      siteId = 'tst';
    }
    // Api info
    vm.apiUrl = '';
    if (window.parent.choirApiV10 != null) {
      vm.apiUrl = window.parent.choirApiV10();
    }
    vm.siteId= siteId;
    // Current patient
    vm.patient = null;
    vm.fullName = '';
    vm.id = '';

    // Patient form and fields
    vm.form = null;
    vm.patientId = '';
    vm.firstName = '';
    vm.lastName = '';
    vm.gender = '';
    vm.dtBirth = null;
    vm.email = '';
    vm.emailValid = '';
    vm.participates = '';
    vm.ethnicity = '';
    vm.created = '';
    vm.changed = '';

    // Exposed methods
    vm.clear = clear;
    vm.show = show;
    vm.reset = reset;
    vm.register = register;
    vm.update = update;



    // If the patient id is set in the parent window then show the patient
    if (window.parent.getPatientId != null) {
      var patientId = window.parent.getPatientId();
      if (patientId != null) {
        choirApi.getPatient(patientId, function(patient) {
          if (patient != null) {
            show(patient);
          }
        });
      }
    }

    /*
     * Clear the page
     */
    function clear() {
      vm.patient = null;
      vm.fullName = '';
      vm.patientId = '';
      vm.firstName = '';
      vm.lastName = '';
      vm.gender = '';
      vm.dtBirth = null;
      vm.email = '';
      vm.emailValid = '';
      vm.participates = '';
      vm.ethnicity = '';
      vm.created = '';
      vm.changed = '';
      vm.pronoun = '';

      // Reset the patient form state
      if (vm.form != null) {
        vm.form.$setUntouched();
        vm.form.$setPristine();
      }
    }

    /*
     * Show the patient
     */
    function show(pat) {
      clear();
      vm.patient = pat;
      if (pat != null) {
        toForm(pat);
      }
    }

    /*
     * Reset the patient form
     */
    function reset() {
      show(vm.patient);
    }

    /*
     * Register the patient
     */
    function register() {
      choirApi.registerPatient(fromForm(), function(pat) {
        show(pat);
      });
    }

    /*
     * Update the patient
     */
    function update() {
      choirApi.updatePatient(fromForm(), function(pat) {
        choirApi.getPatient(pat.patientId, function(pat) {
          show(pat);
        });
      });
    }

    /*
     * Fill in the form fields from the patient
     */
    function toForm(patient) {

      vm.patientId = patient.patientId;
      vm.firstName = patient.firstName;
      vm.lastName = patient.lastName;
      vm.fullName = patient.lastName + ', ' + patient.firstName;
      vm.dtBirth = null;
      if ((patient.dtBirth != null) && (patient.dtBirth != '')) {
        vm.dtBirth = new Date(patient.dtBirth);
      }
      vm.created = new Date(patient.dtCreated);
      if ((patient.dtChanged != null) && (patient.dtChanged != '')) {
        vm.changed = new Date(patient.dtChanged);
      }
      vm.pronoun='Patient';
      for (var i = 0; i < patient.attributes.length; i++) {
        var attr = patient.attributes[i];


        if ((attr.name === 'gender') && (attr.value !== null)) {
          vm.gender = attr.value;
          if (attr.value === 'Male') {
            vm.pronoun = 'He';
          }
          if (attr.value === 'Female') {
            vm.pronoun = 'She';
          }
        }
        if ((attr.name === 'surveyEmailAddressAlt') && (attr.value != null)) {
          vm.email = attr.value;
        }
        if ((attr.name === 'surveyEmailAddressValid') && (attr.value != null)) {
          vm.emailValid = attr.value;
        }
        if ((attr.name === 'participatesInSurveys') && (attr.value != null)) {
          vm.participates = attr.value;
        }
        if ((attr.name === 'ethnicity') && (attr.value != null)) {
          vm.ethnicity = attr.value;
        }
      }
      vm.id = '(' + vm.patientId + ')';
    }

    /*
     * Get the patient from the form fields
     */
    function fromForm() {
      var pat = {
        patientId: vm.patientId,
        firstName: vm.firstName,
        lastName: vm.lastName,
        dtBirth: $filter('date')(vm.dtBirth, 'MM/dd/yyyy'),
        attributes: [
          { name: 'gender',     value: getFieldValue(vm.gender) },
          { name: 'surveyEmailAddressAlt',   value: getFieldValue(vm.email) },
          { name: 'surveyEmailAddressValid', value: getFieldValue(vm.emailValid) },
          { name: 'participatesInSurveys', value: getFieldValue(vm.participates) },
          { name: 'ethnicity', value: getFieldValue(vm.ethnicity)}
        ]
      };

      return pat;
    }

    /*
     * Get the value of a patient attribute
     */
    function getAttr(name) {
      if (vm.patient != null) {
        for (var i = 0; i < vm.patient.attributes.length; i++) {
          var attr = vm.patient.attributes[i];
          if (attr.name == name) {
            return attr.value
          }
        }
      }
      return '';
    }

    function getFieldValue(value) {
      if (value.trim() == '') {
        return null;
      }
      return value.trim();
    }
  }

})();