/*
 * Copyright 2014 The Board of Trustees of The Leland Stanford Junior University.
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

package edu.stanford.survey.server.promis;

import edu.stanford.survey.server.CatAlgorithm.ItemBank;

import java.util.HashMap;
import java.util.Map;

/**
 * Catalog of PROMIS item banks we have implemented, and lookups to access them.
 */
public enum Bank {
  OneZeroSelfEfficacyManageDayActiv {
    @Override
    public ItemBank bank() {
      return PromisBankOneZeroSelfEfficacyManageDayActiv.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Bank v1.0 - Self-Efficacy Manage Day Activ";
    }
  },
  OneZeroSelfEfficacyManageSocInter {
    @Override
    public ItemBank bank() {
      return PromisBankOneZeroSelfEfficacyManageSocInter.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Bank v1.0 - Self-Efficacy Manage Soc Inter";
    }
  },
  OneZeroSelfEfficacyManageSymptoms {
    @Override
    public ItemBank bank() {
      return PromisBankOneZeroSelfEfficacyManageSymptoms.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Bank v1.0 - Self-Efficacy Manage Symptoms";
    }
  },
  OneZeroSelfEfficacyManageEmotions {
    @Override
    public ItemBank bank() {
      return PromisBankOneZeroSelfEfficacyManageEmotions.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Bank v1.0 - Self-Efficacy Manage Emotions";
    }
  },
  OneZeroSelfEfficacyManageMedsTx {
    @Override
    public ItemBank bank() {
      return PromisBankOneZeroSelfEfficacyManageMedsTx.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Bank v1.0 - Self-Efficacy Manage Meds/Tx";
    }
  },
  anger {
    @Override
    public ItemBank bank() {
      return PromisAnger.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Bank v1.0 - Anger";
    }
  },
  anxiety {
    @Override
    public ItemBank bank() {
      return PromisAnxiety2.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Bank v1.0 - Anxiety";
    }
  },
  depression {
    @Override
    public ItemBank bank() {
      return PromisDepression2.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Bank v1.0 - Depression";
    }
  },
  fatigue {
    @Override
    public ItemBank bank() {
      return PromisFatigue2.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Bank v1.0 - Fatigue";
    }
  },
  painBehavior {
    @Override
    public ItemBank bank() {
      return PromisPainBehavior.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Bank v1.0 - Pain Behavior";
    }
  },
  painInterference1 {
    @Override
    public ItemBank bank() { return PromisPainInterference1.bank(); }

    @Override
    public String officialName() {
      return "PROMIS Bank v1.1 - Pain Interference";
    }
  },
  physicalFunction2 {
    @Override
    public ItemBank bank() {
      return PromisPhysicalFunction2.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Bank v1.2 - Physical Function";
    }
  },
  physicalFunctionUpperExtremity {
    @Override
    public ItemBank bank() { return PromisPhysicalFunctionUpperExtremity.bank(); }

    @Override
    public String officialName() {
      return "PROMIS Bank v1.2 - Upper Extremity";
    }
  },
  physicalFunctionMobility {
    @Override
    public ItemBank bank() { return PromisPhysicalFunctionMobility.bank(); }

    @Override
    public String officialName() { return "PROMIS Bank v1.2 - Mobility"; }
  },
  sleepDisturbance {
    @Override
    public ItemBank bank() {
      return PromisSleepDisturbance.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Bank v1.0 - Sleep Disturbance";
    }
  },
  sleepRelatedImpairment {
    @Override
    public ItemBank bank() {
      return PromisSleepRelatedImpairment.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Bank v1.0 - Sleep-Related Impairment";
    }
  },
  abilityToParticipateSocial {
    @Override
    public ItemBank bank() {
      return PromisAbilityToParticipateSocial.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Bank v2.0 - Ability to Participate Social";
    }
  },
  alcoholUse {
    @Override
    public ItemBank bank() {
      return PromisAlcoholUse.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Bank v1.0 Alcohol: Alcohol Use";
    }
  },
  alcoholNegativeConsequences {
    @Override
    public ItemBank bank() {
      return PromisAlcoholNegativeConsequences.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Bank v1.0 Alcohol: Negative Consequences";
    }
  },
  alcoholNegativeExpectancies {
    @Override
    public ItemBank bank() {
      return PromisAlcoholNegativeExpectancies.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Bank v1.0 Alcohol: Negative Expectancies";
    }
  },
  alcoholPositiveConsequences {
    @Override
    public ItemBank bank() {
      return PromisAlcoholPositiveConsequences.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Bank v1.0 Alcohol: Positive Consequences";
    }
  },
  alcoholPositiveExpectancies {
    @Override
    public ItemBank bank() {
      return PromisAlcoholPositiveExpectancies.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Bank v1.0 Alcohol: Positive Expectancies";
    }
  },
  appliedCogAbilities {
    @Override
    public ItemBank bank() {
      return PromisAppliedCogAbilities.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Bank v1.0 - Applied Cog Abilities";
    }
  },
  appliedCogGeneralConcerns {
    @Override
    public ItemBank bank() {
      return PromisAppliedCogGeneralConcerns.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Bank v1.0 - Applied Cog General Concerns";
    }
  },
  socialSatDsa {
    @Override
    public ItemBank bank() {
      return PromisSocialSatDSA.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Bank v1.0 - Social Sat DSA";
    }
  },
  socialSatRole {
    @Override
    public ItemBank bank() {
      return PromisSocialSatRole.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Bank v1.0 - Social Sat Role";
    }
  },
  emotionalSupport {
    @Override
    public ItemBank bank() {
      return PromisEmotionalSupport.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Bank v2.0 - Emotional Support";
    }
  },
  informationalSupport {
    @Override
    public ItemBank bank() {
      return PromisInformationalSupport.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Bank v2.0 - Informational Support";
    }
  },
  instrumentalSupport {
    @Override
    public ItemBank bank() {
      return PromisInstrumentalSupport.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Bank v2.0 - Instrumental Support";
    }
  },
  satisfactionRolesActivities {
    @Override
    public ItemBank bank() {
      return PromisSatisfactionRolesActivities.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Bank v2.0 - Satisfaction Roles Activities";
    }
  },
  socialIsolation {
    @Override
    public ItemBank bank() {
      return PromisSocialIsolation.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Bank v2.0 - Social Isolation";
    }
  },
  parentProxyFatigue {
    @Override
    public ItemBank bank() {
      return PromisParentProxyFatigue.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Parent Proxy Bank v1.0 - Fatigue";
    }
  },
  parentProxyMobility {
    @Override
    public ItemBank bank() {
      return PromisParentProxyMobility.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Parent Proxy Bank v1.0 - Mobility";
    }
  },
  parentProxyPainInterference {
    @Override
    public ItemBank bank() {
      return PromisParentProxyPainInterference.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Parent Proxy Bank v1.0 - Pain Interference";
    }
  },
  parentProxyPeerRelations {
    @Override
    public ItemBank bank() {
      return PromisParentProxyPeerRelations.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Parent Proxy Bank v1.0 - Peer Relations";
    }
  },
  parentProxyAnxiety {
    @Override
    public ItemBank bank() {
      return PromisParentProxyAnxiety.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Parent Proxy Bank v1.1 - Anxiety";
    }
  },
  parentProxyDepressiveSx {
    @Override
    public ItemBank bank() {
      return PromisParentProxyDepressiveSx.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Parent Proxy Bank v1.1 - Depressive Sx";
    }
  },
  pediatricAsthma {
    @Override
    public ItemBank bank() {
      return PromisPediatricAsthma.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Ped Bank v1.0 - Asthma";
    }
  },
  pediatricFatigue {
    @Override
    public ItemBank bank() {
      return PromisPediatricFatigue.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Ped Bank v1.0 - Fatigue";
    }
  },
  pediatricMobility {
    @Override
    public ItemBank bank() {
      return PromisPediatricMobility.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Ped Bank v1.0 - Mobility";
    }
  },
  pediatricPainInterference {
    @Override
    public ItemBank bank() {
      return PromisPediatricPainInterference.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Ped Bank v1.0 - Pain Interference";
    }
  },
  pediatricPeerRelations {
    @Override
    public ItemBank bank() {
      return PromisPediatricPeerRelations.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Ped Bank v1.0 - Peer Rel";
    }
  },
  pediatricAnxiety {
    @Override
    public ItemBank bank() {
      return PromisPediatricAnxiety.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Ped Bank v1.1 - Anxiety";
    }
  },
  pediatricDepressiveSx {
    @Override
    public ItemBank bank() {
      return PromisPediatricDepressiveSx.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Ped Bank v1.1 - Depressive Sx";
    }
  },
  promisParentProxyShortFormOneZeroPainInterfere8a {
    @Override
    public ItemBank bank() {
      return PromisParentProxyShortFormOneZeroPainInterfere8a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Parent Proxy SF v1.0 - Pain Interfere 8a";
    }
  },
  promisShortFormOneZeroAnsiedad6a {
    @Override
    public ItemBank bank() {
      return PromisShortFormOneZeroAnsiedad6a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0 - Ansiedad 6a";
    }
  },
  promisBankOneZeroSmokingCopingExpectAllSmk {
    @Override
    public ItemBank bank() {
      return PromisBankOneZeroSmokingCopingExpectAllSmk.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Bank v1.0 - Smoking Coping Expect AllSmk";
    }
  },
  promisShortFormOneZeroAgotamiento8a {
    @Override
    public ItemBank bank() {
      return PromisShortFormOneZeroAgotamiento8a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0 - Agotamiento 8a";
    }
  },
  promisShortFormOneZeroSelfEfficacyManageEmotions4a {
    @Override
    public ItemBank bank() {
      return PromisShortFormOneZeroSelfEfficacyManageEmotions4a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0 - Self-Efficacy Manage Emotions 4a";
    }
  },
  neuroQoLShortFormPedOneZeroDolor {
    @Override
    public ItemBank bank() {
      return NeuroQoLShortFormPedOneZeroDolor.bank();
    }

    @Override
    public String officialName() {
      return "Neuro-QoL SF Ped  v1.0 - Dolor";
    }
  },
  neuroQoLShortFormPedOneZeroEstigma {
    @Override
    public ItemBank bank() {
      return NeuroQoLShortFormPedOneZeroEstigma.bank();
    }

    @Override
    public String officialName() {
      return "Neuro-QoL SF Ped  v1.0 - Estigma";
    }
  },
  promisBankOneZeroSmokingHealthExpectNonDaily {
    @Override
    public ItemBank bank() {
      return PromisBankOneZeroSmokingHealthExpectNonDaily.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Bank v1.0 - Smoking Health Expect NonDaily";
    }
  },
  promisParentProxyShortFormOneZeroAsthma8a {
    @Override
    public ItemBank bank() {
      return PromisParentProxyShortFormOneZeroAsthma8a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Parent Proxy SF v1.0 - Asthma 8a";
    }
  },
  neuroQoLBancoPedOneZeroEstigma {
    @Override
    public ItemBank bank() {
      return NeuroQoLBancoPedOneZeroEstigma.bank();
    }

    @Override
    public String officialName() {
      return "Neuro-QoL Banco Ped  v1.0 - Estigma";
    }
  },
  promisShortFormOneZeroSatisfaccinConLaParticipacinEnRolesSociales7a {
    @Override
    public ItemBank bank() {
      return PromisShortFormOneZeroSatisfaccinConLaParticipacinEnRolesSociales7a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0 - Satisfacción con la participación en roles sociales 7a";
    }
  },
  neuroQolPedBankOneZeroAnxiety {
    @Override
    public ItemBank bank() {
      return NeuroQolPedBankOneZeroAnxiety.bank();
    }

    @Override
    public String officialName() {
      return "Neuro-QOL Ped Bank v1.0 - Anxiety";
    }
  },
  promisShortFormOneZeroAppliedCogGeneralConcerns6a {
    @Override
    public ItemBank bank() {
      return PromisShortFormOneZeroAppliedCogGeneralConcerns6a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0 - Applied Cog General Concerns 6a";
    }
  },
  promisShortFormOneZeroSelfEfficacyManageDayActiv8a {
    @Override
    public ItemBank bank() {
      return PromisShortFormOneZeroSelfEfficacyManageDayActiv8a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0 - Self-Efficacy Manage Day Activ 8a";
    }
  },
  neuroQolPedBankOneOneDepression {
    @Override
    public ItemBank bank() {
      return NeuroQolPedBankOneOneDepression.bank();
    }

    @Override
    public String officialName() {
      return "Neuro-QOL Ped Bank v1.1 - Depression";
    }
  },
  promisPedBankOneZeroPsychStressExperiences {
    @Override
    public ItemBank bank() {
      return PromisPedBankOneZeroPsychStressExperiences.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Ped Bank v1.0 - Psych Stress Experiences";
    }
  },
  promisParentProxyBankOneZeroPositiveAffect {
    @Override
    public ItemBank bank() {
      return PromisParentProxyBankOneZeroPositiveAffect.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Parent Proxy Bank v1.0 - Positive Affect";
    }
  },
  neuroQolBankOneZeroDepression {
    @Override
    public ItemBank bank() {
      return NeuroQolBankOneZeroDepression.bank();
    }

    @Override
    public String officialName() {
      return "Neuro-QOL Bank v1.0 - Depression";
    }
  },
  promisShortFormV10Depression4a {
    @Override
    public ItemBank bank() {
      return PromisShortFormV10Depression4a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0-Depression 4a";
    }
  },
  promisParentProxyShortFormTwoZeroPeerRelations7a {
    @Override
    public ItemBank bank() {
      return PromisParentProxyShortFormTwoZeroPeerRelations7a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Parent Proxy SF v2.0 - Peer Relations 7a";
    }
  },
  promisShortFormOneZeroSmokingNicotineDependNonday4a {
    @Override
    public ItemBank bank() {
      return PromisShortFormOneZeroSmokingNicotineDependNonday4a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0 - Smoking NicotineDepend Nonday 4a";
    }
  },
  promisShortFormV10Anxiety4a {
    @Override
    public ItemBank bank() {
      return PromisShortFormV10Anxiety4a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0-Anxiety 4a";
    }
  },
  promisShortFormOneZeroEfectosDelDolor6b {
    @Override
    public ItemBank bank() {
      return PromisShortFormOneZeroEfectosDelDolor6b.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0 - Efectos del dolor  6b";
    }
  },
  promisShortFormOneZeroEfectosDelDolor6a {
    @Override
    public ItemBank bank() {
      return PromisShortFormOneZeroEfectosDelDolor6a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0 - Efectos del dolor  6a";
    }
  },
  promisShortFormOneZeroAnsiedad7a {
    @Override
    public ItemBank bank() {
      return PromisShortFormOneZeroAnsiedad7a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0 - Ansiedad 7a";
    }
  },
  promisShortFormOneZeroSmokingSocialMotiveNonday4a {
    @Override
    public ItemBank bank() {
      return PromisShortFormOneZeroSmokingSocialMotiveNonday4a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0 - Smoking Social Motive Nonday 4a";
    }
  },
  promisShortFormOneZeroSmokingEmotSensoryDay6a {
    @Override
    public ItemBank bank() {
      return PromisShortFormOneZeroSmokingEmotSensoryDay6a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0 - Smoking Emot/Sensory Day 6a";
    }
  },
  neuroQolShortFormTwoZeroCognitiveFunction {
    @Override
    public ItemBank bank() {
      return NeuroQolShortFormTwoZeroCognitiveFunction.bank();
    }

    @Override
    public String officialName() {
      return "Neuro-QOL SF v2.0 - Cognitive Function";
    }
  },
  promisShortFormOneZeroPainInterference6b {
    @Override
    public ItemBank bank() {
      return PromisShortFormOneZeroPainInterference6b.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0 - Pain Interference 6b";
    }
  },
  promisParentProxyShortFormOneZeroPainBehavior8a {
    @Override
    public ItemBank bank() {
      return PromisParentProxyShortFormOneZeroPainBehavior8a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Parent Proxy SF v1.0 - Pain Behavior 8a";
    }
  },
  neuroQolShortFormOneZeroFatigue {
    @Override
    public ItemBank bank() {
      return NeuroQolShortFormOneZeroFatigue.bank();
    }

    @Override
    public String officialName() {
      return "Neuro-QOL SF v1.0 - Fatigue";
    }
  },
  promisShortFormOneZeroSatisfaccinConLaParticipacinEnRolesSociales8a {
    @Override
    public ItemBank bank() {
      return PromisShortFormOneZeroSatisfaccinConLaParticipacinEnRolesSociales8a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0 - Satisfacción con la participación en roles sociales 8a";
    }
  },
  promisShortFormTwoZeroEmotionalSupport8a {
    @Override
    public ItemBank bank() {
      return PromisShortFormTwoZeroEmotionalSupport8a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v2.0 - Emotional Support 8a";
    }
  },
  promisShortFormOneZeroAnxiety7a {
    @Override
    public ItemBank bank() {
      return PromisShortFormOneZeroAnxiety7a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0 - Anxiety 7a";
    }
  },
  promisShortFormOneZeroComportamientoAnteElDolor7a {
    @Override
    public ItemBank bank() {
      return PromisShortFormOneZeroComportamientoAnteElDolor7a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0 - Comportamiento ante el dolor  7a";
    }
  },
  neuroQoLBancoPedOneOneDepresin {
    @Override
    public ItemBank bank() {
      return NeuroQoLBancoPedOneOneDepresin.bank();
    }

    @Override
    public String officialName() {
      return "Neuro-QoL Banco Ped  v1.1 - Depresión";
    }
  },
  promisShortFormTwoZeroSatisfactionRolesActivities4a {
    @Override
    public ItemBank bank() {
      return PromisShortFormTwoZeroSatisfactionRolesActivities4a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v2.0 - Satisfaction Roles Activities 4a";
    }
  },
  promisShortFormOneTwoPhysicalFunction8b {
    @Override
    public ItemBank bank() {
      return PromisShortFormOneTwoPhysicalFunction8b.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.2 - Physical Function 8b";
    }
  },
  neuroQoLShortFormPedOneZeroDepresin {
    @Override
    public ItemBank bank() {
      return NeuroQoLShortFormPedOneZeroDepresin.bank();
    }

    @Override
    public String officialName() {
      return "Neuro-QoL SF Ped  v1.0 - Depresión";
    }
  },
  promisParentProxyShortFormOneZeroPsychStressExp8a {
    @Override
    public ItemBank bank() {
      return PromisParentProxyShortFormOneZeroPsychStressExp8a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Parent Proxy SF v1.0 - Psych Stress Exp 8a";
    }
  },
  promisShortFormV10PhysicalFunction4a {
    @Override
    public ItemBank bank() {
      return PromisShortFormV10PhysicalFunction4a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0-Physical Function 4a";
    }
  },
  promisShortFormOneZeroSmokingCopingExpectNonday4a {
    @Override
    public ItemBank bank() {
      return PromisShortFormOneZeroSmokingCopingExpectNonday4a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0 - Smoking Coping Expect Nonday 4a";
    }
  },
  promisParentProxyShortFormTwoZeroUpperExtremity8a {
    @Override
    public ItemBank bank() {
      return PromisParentProxyShortFormTwoZeroUpperExtremity8a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Parent Proxy SF v2.0 - Upper Extremity 8a";
    }
  },
  promisParentProxyShortFormOneZeroPhysicalActivity8a {
    @Override
    public ItemBank bank() {
      return PromisParentProxyShortFormOneZeroPhysicalActivity8a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Parent Proxy SF v1.0 - Physical Activity 8a";
    }
  },
  promisShortFormOneZeroSocialSatDSA7a {
    @Override
    public ItemBank bank() {
      return PromisShortFormOneZeroSocialSatDSA7a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0 - Social Sat DSA 7a";
    }
  },
  neuroQoLShortFormPedOneZeroEnojo {
    @Override
    public ItemBank bank() {
      return NeuroQoLShortFormPedOneZeroEnojo.bank();
    }

    @Override
    public String officialName() {
      return "Neuro-QoL SF Ped  v1.0 - Enojo";
    }
  },
  promisShortFormOneZeroAgotamiento6a {
    @Override
    public ItemBank bank() {
      return PromisShortFormOneZeroAgotamiento6a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0 - Agotamiento 6a";
    }
  },
  promisShortFormOneZeroAnsiedad8a {
    @Override
    public ItemBank bank() {
      return PromisShortFormOneZeroAnsiedad8a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0 - Ansiedad 8a";
    }
  },
  promisBancoOneOneEnojo {
    @Override
    public ItemBank bank() {
      return PromisBancoOneOneEnojo.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Banco v1.1 - Enojo";
    }
  },
  promisParentProxyShortFormOneZeroPositiveAffect4a {
    @Override
    public ItemBank bank() {
      return PromisParentProxyShortFormOneZeroPositiveAffect4a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Parent Proxy SF v1.0 - Positive Affect 4a";
    }
  },
  promisParentProxyBankTwoZeroPeerRelations {
    @Override
    public ItemBank bank() {
      return PromisParentProxyBankTwoZeroPeerRelations.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Parent Proxy Bank v2.0 - Peer Relations";
    }
  },
  promisShortFormOneZeroAlcoholNegativeExpectancies7a {
    @Override
    public ItemBank bank() {
      return PromisShortFormOneZeroAlcoholNegativeExpectancies7a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0 Alcohol: Negative Expectancies 7a";
    }
  },
  promisParentProxyShortFormOneZeroLifeSatisfaction4a {
    @Override
    public ItemBank bank() {
      return PromisParentProxyShortFormOneZeroLifeSatisfaction4a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Parent Proxy SF v1.0 - Life Satisfaction 4a";
    }
  },
  promisBancoOneZeroAnsiedad {
    @Override
    public ItemBank bank() {
      return PromisBancoOneZeroAnsiedad.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Banco v1.0 - Ansiedad";
    }
  },
  neuroQoLShortFormTwoZeroFuncinCognitiva {
    @Override
    public ItemBank bank() {
      return NeuroQoLShortFormTwoZeroFuncinCognitiva.bank();
    }

    @Override
    public String officialName() {
      return "Neuro-QoL SF v2.0 - Función cognitiva";
    }
  },
  promisParentProxyShortFormOneZeroStrengthImpact4a {
    @Override
    public ItemBank bank() {
      return PromisParentProxyShortFormOneZeroStrengthImpact4a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Parent Proxy SF v1.0 - Strength Impact 4a";
    }
  },
  neuroQoLShortFormOneZeroCapacidadParaParticiparEnRolesYActividadesSociales {
    @Override
    public ItemBank bank() {
      return NeuroQoLShortFormOneZeroCapacidadParaParticiparEnRolesYActividadesSociales.bank();
    }

    @Override
    public String officialName() {
      return "Neuro-QoL SF v1.0 - Capacidad para participar en roles y actividades sociales";
    }
  },
  promisPediatricScaleOneZeroGlobalHealth7 {
    @Override
    public ItemBank bank() {
      return PromisPediatricScaleOneZeroGlobalHealth7.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Pediatric Scale v1.0 - Global Health 7";
    }
  },
  promisShortFormOneZeroAppliedCogGeneralConcerns4a {
    @Override
    public ItemBank bank() {
      return PromisShortFormOneZeroAppliedCogGeneralConcerns4a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0 - Applied Cog General Concerns 4a";
    }
  },
  neuroQolPedShortFormOneZeroSRInteractionWPeers {
    @Override
    public ItemBank bank() {
      return NeuroQolPedShortFormOneZeroSRInteractionWPeers.bank();
    }

    @Override
    public String officialName() {
      return "Neuro-QOL Ped SF v1.0 - SR - Interaction w Peers";
    }
  },
  promisParentProxyShortFormV20DepressiveSymptoms6a {
    @Override
    public ItemBank bank() {
      return PromisParentProxyShortFormV20DepressiveSymptoms6a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Parent Proxy SF v2.0-Depressive Symptoms 6a";
    }
  },
  promisShortFormV10SocialSatRole4a {
    @Override
    public ItemBank bank() {
      return PromisShortFormV10SocialSatRole4a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0-Social Sat Role 4a";
    }
  },
  promisShortFormV10Depression6a {
    @Override
    public ItemBank bank() {
      return PromisShortFormV10Depression6a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0-Depression 6a";
    }
  },
  promisShortFormOneZeroSmokingNicotineDepend8aAllSmk {
    @Override
    public ItemBank bank() {
      return PromisShortFormOneZeroSmokingNicotineDepend8aAllSmk.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0 - Smoking NicotineDepend 8a AllSmk";
    }
  },
  promisShortFormOneZeroSocialSatRole7a {
    @Override
    public ItemBank bank() {
      return PromisShortFormOneZeroSocialSatRole7a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0 - Social Sat Role 7a";
    }
  },
  promisParentProxyShortFormOneZeroFatigue10a {
    @Override
    public ItemBank bank() {
      return PromisParentProxyShortFormOneZeroFatigue10a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Parent Proxy SF v1.0 - Fatigue 10a";
    }
  },
  promisPedBankOneZeroLifeSatisfaction {
    @Override
    public ItemBank bank() {
      return PromisPedBankOneZeroLifeSatisfaction.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Ped Bank v1.0 - Life Satisfaction";
    }
  },
  promisShortFormV10Anxiety6a {
    @Override
    public ItemBank bank() {
      return PromisShortFormV10Anxiety6a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0-Anxiety 6a";
    }
  },
  promisBankOneOneAnger {
    @Override
    public ItemBank bank() {
      return PromisBankOneOneAnger.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Bank v1.1 - Anger";
    }
  },
  promisShortFormTwoZeroCogFunctionAbilitiesSubset8a {
    @Override
    public ItemBank bank() {
      return PromisShortFormTwoZeroCogFunctionAbilitiesSubset8a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v2.0 - Cog Function Abilities Subset 8a";
    }
  },
  promisParentProxyShortFormTwoZeroAsthmaImpact8a {
    @Override
    public ItemBank bank() {
      return PromisParentProxyShortFormTwoZeroAsthmaImpact8a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Parent Proxy SF v2.0 - Asthma Impact 8a";
    }
  },
  promisBankOneZeroSmokingNicotineDependDaily {
    @Override
    public ItemBank bank() {
      return PromisBankOneZeroSmokingNicotineDependDaily.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Bank v1.0 - Smoking NicotineDepend Daily";
    }
  },
  promisShortFormOneZeroAgotamiento7a {
    @Override
    public ItemBank bank() {
      return PromisShortFormOneZeroAgotamiento7a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0 - Agotamiento 7a";
    }
  },
  promisShortFormOneZeroEfectosDelDolor8a {
    @Override
    public ItemBank bank() {
      return PromisShortFormOneZeroEfectosDelDolor8a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0 - Efectos del dolor  8a";
    }
  },
  promisShortFormOneZeroSmokingPsychSocExpectDay6a {
    @Override
    public ItemBank bank() {
      return PromisShortFormOneZeroSmokingPsychSocExpectDay6a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0 - Smoking PsychSocExpect Day 6a";
    }
  },
  neuroQoLBancoPedOneZeroRelacionesSocialesInteraccinConNiosDeLaMismaEdad {
    @Override
    public ItemBank bank() {
      return NeuroQoLBancoPedOneZeroRelacionesSocialesInteraccinConNiosDeLaMismaEdad.bank();
    }

    @Override
    public String officialName() {
      return "Neuro-QoL Banco Ped  v1.0 - Relaciones sociales: Interacción con niños de la misma edad";
    }
  },
  neuroQolBankOneZeroStigma {
    @Override
    public ItemBank bank() {
      return NeuroQolBankOneZeroStigma.bank();
    }

    @Override
    public String officialName() {
      return "Neuro-QOL Bank v1.0 - Stigma";
    }
  },
  promisBankOneZeroSmokingSocialMotiveAllSmk {
    @Override
    public ItemBank bank() {
      return PromisBankOneZeroSmokingSocialMotiveAllSmk.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Bank v1.0 - Smoking Social Motive AllSmk";
    }
  },
  promisCaBankOneZeroDepression {
    @Override
    public ItemBank bank() {
      return PromisCaBankOneZeroDepression.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS-Ca Bank v1.0 - Depression";
    }
  },
  promisPedBankOneZeroPainBehavior {
    @Override
    public ItemBank bank() {
      return PromisPedBankOneZeroPainBehavior.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Ped Bank v1.0 - Pain Behavior";
    }
  },
  promisShortFormTwoZeroSatisfactionRolesActivities6a {
    @Override
    public ItemBank bank() {
      return PromisShortFormTwoZeroSatisfactionRolesActivities6a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v2.0 - Satisfaction Roles Activities 6a";
    }
  },
  neuroQolShortFormOneZeroAbilityToPartInSRA {
    @Override
    public ItemBank bank() {
      return NeuroQolShortFormOneZeroAbilityToPartInSRA.bank();
    }

    @Override
    public String officialName() {
      return "Neuro-QOL SF v1.0 - Ability to Part. in SRA";
    }
  },
  promisShortFormOneZeroSleepRelatedImpairment8a {
    @Override
    public ItemBank bank() {
      return PromisShortFormOneZeroSleepRelatedImpairment8a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0 - Sleep-Related Impairment 8a";
    }
  },
  neuroQolShortFormOneZeroSleepDisturbance {
    @Override
    public ItemBank bank() {
      return NeuroQolShortFormOneZeroSleepDisturbance.bank();
    }

    @Override
    public String officialName() {
      return "Neuro-QOL SF v1.0 - Sleep Disturbance";
    }
  },
  promisBancoOneZeroSatisfaccinConLaParticipacinEnRolesSociales {
    @Override
    public ItemBank bank() {
      return PromisBancoOneZeroSatisfaccinConLaParticipacinEnRolesSociales.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Banco v1.0 - Satisfacción con la participación en roles sociales";
    }
  },
  neuroQoLBancoOneZeroSentimientosPositivosYBienestar {
    @Override
    public ItemBank bank() {
      return NeuroQoLBancoOneZeroSentimientosPositivosYBienestar.bank();
    }

    @Override
    public String officialName() {
      return "Neuro-QoL Banco v1.0 - Sentimientos positivos y bienestar";
    }
  },
  promisShortFormOneZeroAgotamiento4a {
    @Override
    public ItemBank bank() {
      return PromisShortFormOneZeroAgotamiento4a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0 - Agotamiento 4a";
    }
  },
  promisShortFormOneTwoPhysicalFunction6b {
    @Override
    public ItemBank bank() {
      return PromisShortFormOneTwoPhysicalFunction6b.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.2 - Physical Function 6b";
    }
  },
  neuroQolShortFormOneZeroAnxiety {
    @Override
    public ItemBank bank() {
      return NeuroQolShortFormOneZeroAnxiety.bank();
    }

    @Override
    public String officialName() {
      return "Neuro-QOL SF v1.0 - Anxiety";
    }
  },
  neuroQoLShortFormOneZeroDescontrolEmocionalYConductual {
    @Override
    public ItemBank bank() {
      return NeuroQoLShortFormOneZeroDescontrolEmocionalYConductual.bank();
    }

    @Override
    public String officialName() {
      return "Neuro-QoL SF v1.0 - Descontrol emocional y conductual";
    }
  },
  promisPedShortFormOneZeroPsychStressExperiences8a {
    @Override
    public ItemBank bank() {
      return PromisPedShortFormOneZeroPsychStressExperiences8a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Ped SF v1.0 - Psych Stress Experiences 8a";
    }
  },
  promisBankOneZeroSmokingEmotSensoryAllSmk {
    @Override
    public ItemBank bank() {
      return PromisBankOneZeroSmokingEmotSensoryAllSmk.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Bank v1.0 - Smoking Emot/Sensory AllSmk";
    }
  },
  promisShortFormOneZeroAlcoholPositiveExpectancies7a {
    @Override
    public ItemBank bank() {
      return PromisShortFormOneZeroAlcoholPositiveExpectancies7a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0 Alcohol: Positive Expectancies 7a";
    }
  },
  neuroQolShortFormOneZeroDepression {
    @Override
    public ItemBank bank() {
      return NeuroQolShortFormOneZeroDepression.bank();
    }

    @Override
    public String officialName() {
      return "Neuro-QOL SF v1.0 - Depression";
    }
  },
  neuroQolPedShortFormOneZeroStigma {
    @Override
    public ItemBank bank() {
      return NeuroQolPedShortFormOneZeroStigma.bank();
    }

    @Override
    public String officialName() {
      return "Neuro-QOL Ped SF v1.0 - Stigma";
    }
  },
  promisParentProxyShortFormOneZeroPhysStressExp4a {
    @Override
    public ItemBank bank() {
      return PromisParentProxyShortFormOneZeroPhysStressExp4a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Parent Proxy SF v1.0 - Phys Stress Exp 4a";
    }
  },
  promisParentProxyBankOneZeroAsthma {
    @Override
    public ItemBank bank() {
      return PromisParentProxyBankOneZeroAsthma.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Parent Proxy Bank v1.0 - Asthma";
    }
  },
  promisShortFormOneZeroSmokingNicotineDependDay8a {
    @Override
    public ItemBank bank() {
      return PromisShortFormOneZeroSmokingNicotineDependDay8a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0 - Smoking NicotineDepend Day 8a";
    }
  },
  promisShortFormV10Depression8a {
    @Override
    public ItemBank bank() {
      return PromisShortFormV10Depression8a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0-Depression 8a";
    }
  },
  promisShortFormOneZeroSleepDisturb4a {
    @Override
    public ItemBank bank() {
      return PromisShortFormOneZeroSleepDisturb4a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0 - Sleep Disturb 4a";
    }
  },
  promisShortFormOneZeroSleepDisturb8b {
    @Override
    public ItemBank bank() {
      return PromisShortFormOneZeroSleepDisturb8b.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0 - Sleep Disturb 8b";
    }
  },
  promisShortFormOneZeroDepresin4a {
    @Override
    public ItemBank bank() {
      return PromisShortFormOneZeroDepresin4a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0 - Depresión 4a";
    }
  },
  promisShortFormTwoZeroInformationalSupport6a {
    @Override
    public ItemBank bank() {
      return PromisShortFormTwoZeroInformationalSupport6a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v2.0 - Informational Support 6a";
    }
  },
  promisShortFormTwoZeroCompanionship4a {
    @Override
    public ItemBank bank() {
      return PromisShortFormTwoZeroCompanionship4a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v2.0 - Companionship 4a";
    }
  },
  promisPedBankOneZeroMeaningAndPurpose {
    @Override
    public ItemBank bank() {
      return PromisPedBankOneZeroMeaningAndPurpose.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Ped Bank v1.0 - Meaning and Purpose";
    }
  },
  neuroQoLShortFormPedV21Agotamiento {
    @Override
    public ItemBank bank() {
      return NeuroQoLShortFormPedV21Agotamiento.bank();
    }

    @Override
    public String officialName() {
      return "Neuro-QoL SF Ped  v2.1 - Agotamiento";
    }
  },
  promisParentProxyShortFormOneZeroStrengthImpact8a {
    @Override
    public ItemBank bank() {
      return PromisParentProxyShortFormOneZeroStrengthImpact8a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Parent Proxy SF v1.0 - Strength Impact 8a";
    }
  },
  neuroQolShortFormOneZeroEmotionalBehDyscontrol {
    @Override
    public ItemBank bank() {
      return NeuroQolShortFormOneZeroEmotionalBehDyscontrol.bank();
    }

    @Override
    public String officialName() {
      return "Neuro-QOL SF v1.0 - Emotional & Beh. Dyscontrol";
    }
  },
  neuroQolShortFormOneZeroStigma {
    @Override
    public ItemBank bank() {
      return NeuroQolShortFormOneZeroStigma.bank();
    }

    @Override
    public String officialName() {
      return "Neuro-QOL SF v1.0 - Stigma";
    }
  },
  promisShortFormV10Anxiety8a {
    @Override
    public ItemBank bank() {
      return PromisShortFormV10Anxiety8a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0-Anxiety 8a";
    }
  },
  promisPedShortFormOneZeroPainInterference8a {
    @Override
    public ItemBank bank() {
      return PromisPedShortFormOneZeroPainInterference8a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Ped SF v1.0 - Pain Interference 8a";
    }
  },
  neuroQolBankTwoZeroCognitiveFunction {
    @Override
    public ItemBank bank() {
      return NeuroQolBankTwoZeroCognitiveFunction.bank();
    }

    @Override
    public String officialName() {
      return "Neuro-QOL Bank v2.0 - Cognitive Function";
    }
  },
  promisParentProxyBankTwoZeroDepressiveSx {
    @Override
    public ItemBank bank() {
      return PromisParentProxyBankTwoZeroDepressiveSx.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Parent Proxy Bank v2.0 - Depressive Sx";
    }
  },
  promisShortFormTwoZeroCogFunctionAbilitiesSubset6a {
    @Override
    public ItemBank bank() {
      return PromisShortFormTwoZeroCogFunctionAbilitiesSubset6a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v2.0 - Cog Function Abilities Subset 6a";
    }
  },
  promisParentProxyScaleTwoZeroAnger {
    @Override
    public ItemBank bank() {
      return PromisParentProxyScaleTwoZeroAnger.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Parent Proxy Scale v2.0 - Anger";
    }
  },
  promisShortFormOneZeroPhysFunction20a {
    @Override
    public ItemBank bank() {
      return PromisShortFormOneZeroPhysFunction20a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0 - Phys. Function 20a";
    }
  },
  neuroQoLBancoOneZeroEstigma {
    @Override
    public ItemBank bank() {
      return NeuroQoLBancoOneZeroEstigma.bank();
    }

    @Override
    public String officialName() {
      return "Neuro-QoL Banco v1.0 - Estigma";
    }
  },
  promisShortFormOneZeroSelfEfficacyManageEmotions8a {
    @Override
    public ItemBank bank() {
      return PromisShortFormOneZeroSelfEfficacyManageEmotions8a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0 - Self-Efficacy Manage Emotions 8a";
    }
  },
  promisShortFormOneZeroSatisfaccinConLaParticipacinEnRolesSociales4a {
    @Override
    public ItemBank bank() {
      return PromisShortFormOneZeroSatisfaccinConLaParticipacinEnRolesSociales4a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0 - Satisfacción con la participación en roles sociales 4a";
    }
  },
  promisShortFormTwoZeroSatisfactionRolesActivities8a {
    @Override
    public ItemBank bank() {
      return PromisShortFormTwoZeroSatisfactionRolesActivities8a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v2.0 - Satisfaction Roles Activities 8a";
    }
  },
  neuroQolBankOneZeroPosAffectWellBeing {
    @Override
    public ItemBank bank() {
      return NeuroQolBankOneZeroPosAffectWellBeing.bank();
    }

    @Override
    public String officialName() {
      return "Neuro-QOL Bank v1.0 Pos. Affect & Well-Being";
    }
  },
  promisPedBankOneZeroStrengthImpact {
    @Override
    public ItemBank bank() {
      return PromisPedBankOneZeroStrengthImpact.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Ped Bank v1.0 - Strength Impact";
    }
  },
  promisPedShortFormOneZeroPositiveAffect8a {
    @Override
    public ItemBank bank() {
      return PromisPedShortFormOneZeroPositiveAffect8a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Ped SF v1.0 - Positive Affect 8a";
    }
  },
  neuroQolShortFormPedOneZeroRelacionesSocialesInteraccinConNiosDeLaMismaEdad {
    @Override
    public ItemBank bank() {
      return NeuroQolShortFormPedOneZeroRelacionesSocialesInteraccinConNiosDeLaMismaEdad.bank();
    }

    @Override
    public String officialName() {
      return "Neuro-QOL SF Ped  v1.0 - Relaciones sociales: Interacción con niños de la misma edad";
    }
  },
  promisParentProxyShortFormOneZeroLifeSatisfaction8a {
    @Override
    public ItemBank bank() {
      return PromisParentProxyShortFormOneZeroLifeSatisfaction8a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Parent Proxy SF v1.0 - Life Satisfaction 8a";
    }
  },
  promisShortFormOneOneEnojo5a {
    @Override
    public ItemBank bank() {
      return PromisShortFormOneOneEnojo5a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.1 - Enojo 5a";
    }
  },
  promisParentProxyShortFormOneZeroPsychStressExp4a {
    @Override
    public ItemBank bank() {
      return PromisParentProxyShortFormOneZeroPsychStressExp4a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Parent Proxy SF v1.0 - Psych Stress Exp 4a";
    }
  },
  neuroQoLBancoTwoZeroFuncinCognitiva {
    @Override
    public ItemBank bank() {
      return NeuroQoLBancoTwoZeroFuncinCognitiva.bank();
    }

    @Override
    public String officialName() {
      return "Neuro-QoL Banco v2.0 - Función Cognitiva";
    }
  },
  neuroQolPedBankOneZeroSRInteractionWPeers {
    @Override
    public ItemBank bank() {
      return NeuroQolPedBankOneZeroSRInteractionWPeers.bank();
    }

    @Override
    public String officialName() {
      return "Neuro-QOL Ped Bank v1.0 - SR - Interaction w Peers";
    }
  },
  promisParentProxyShortFormTwoZeroMobility8a {
    @Override
    public ItemBank bank() {
      return PromisParentProxyShortFormTwoZeroMobility8a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Parent Proxy SF v2.0 - Mobility 8a";
    }
  },
  promisParentProxyShortFormOneZeroPeerRelations7a {
    @Override
    public ItemBank bank() {
      return PromisParentProxyShortFormOneZeroPeerRelations7a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Parent Proxy SF v1.0 - Peer Relations 7a";
    }
  },
  promisParentProxyScaleOneZeroGlobalHealth7 {
    @Override
    public ItemBank bank() {
      return PromisParentProxyScaleOneZeroGlobalHealth7.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Parent Proxy Scale v1.0 - Global Health 7";
    }
  },
  promisParentProxyBankOneZeroStrengthImpact {
    @Override
    public ItemBank bank() {
      return PromisParentProxyBankOneZeroStrengthImpact.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Parent Proxy Bank v1.0 - Strength Impact";
    }
  },
  promisBankOneZeroSmokingHealthExpectAllSmk {
    @Override
    public ItemBank bank() {
      return PromisBankOneZeroSmokingHealthExpectAllSmk.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Bank v1.0 - Smoking Health Expect AllSmk";
    }
  },
  promisBankOneZeroSmokingPsychSocExpectNonDaily {
    @Override
    public ItemBank bank() {
      return PromisBankOneZeroSmokingPsychSocExpectNonDaily.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Bank v1.0 - Smoking PsychSocExpect NonDaily";
    }
  },
  promisBankOneZeroSmokingEmotSensoryNonDaily {
    @Override
    public ItemBank bank() {
      return PromisBankOneZeroSmokingEmotSensoryNonDaily.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Bank v1.0 - Smoking Emot/Sensory NonDaily";
    }
  },
  promisParentProxyShortFormOneZeroPhysicalActivity4a {
    @Override
    public ItemBank bank() {
      return PromisParentProxyShortFormOneZeroPhysicalActivity4a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Parent Proxy SF v1.0 - Physical Activity 4a";
    }
  },
  promisShortFormOneZeroAppliedCogGeneralConcerns8a {
    @Override
    public ItemBank bank() {
      return PromisShortFormOneZeroAppliedCogGeneralConcerns8a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0 - Applied Cog General Concerns 8a";
    }
  },
  neuroQoLShortFormOneZeroSentimientosPositivosYBienestar {
    @Override
    public ItemBank bank() {
      return NeuroQoLShortFormOneZeroSentimientosPositivosYBienestar.bank();
    }

    @Override
    public String officialName() {
      return "Neuro-QoL SF v1.0 - Sentimientos positivos y bienestar";
    }
  },
  promisPedShortFormOneZeroPhysicalStressExperience8a {
    @Override
    public ItemBank bank() {
      return PromisPedShortFormOneZeroPhysicalStressExperience8a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Ped SF v1.0 - Physical Stress Experience 8a";
    }
  },
  neuroQoLBancoOneZeroAnsiedad {
    @Override
    public ItemBank bank() {
      return NeuroQoLBancoOneZeroAnsiedad.bank();
    }

    @Override
    public String officialName() {
      return "Neuro-QoL Banco v1.0 - Ansiedad";
    }
  },
  promisShortFormTwoZeroInformationalSupport4a {
    @Override
    public ItemBank bank() {
      return PromisShortFormTwoZeroInformationalSupport4a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v2.0 - Informational Support 4a";
    }
  },
  neuroQoLBancoOneZeroCapacidadParaParticiparEnRolesYActividadesSociales {
    @Override
    public ItemBank bank() {
      return NeuroQoLBancoOneZeroCapacidadParaParticiparEnRolesYActividadesSociales.bank();
    }

    @Override
    public String officialName() {
      return "Neuro-QoL Banco v1.0 - Capacidad para participar en roles y actividades sociales";
    }
  },
  promisShortFormV10Fatigue8a {
    @Override
    public ItemBank bank() {
      return PromisShortFormV10Fatigue8a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0-Fatigue 8a";
    }
  },
  promisShortFormOneZeroSmokingSocialMotive4aAllSmk {
    @Override
    public ItemBank bank() {
      return PromisShortFormOneZeroSmokingSocialMotive4aAllSmk.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0 - Smoking Social Motive 4a AllSmk";
    }
  },
  promisShortFormOneZeroEfectosDelDolor4a {
    @Override
    public ItemBank bank() {
      return PromisShortFormOneZeroEfectosDelDolor4a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0 - Efectos del dolor  4a";
    }
  },
  promisShortFormTwoZeroCogFunctionAbilitiesSubset4a {
    @Override
    public ItemBank bank() {
      return PromisShortFormTwoZeroCogFunctionAbilitiesSubset4a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v2.0 - Cog Function Abilities Subset 4a";
    }
  },
  neuroQoLBancoOneZeroDepresin {
    @Override
    public ItemBank bank() {
      return NeuroQoLBancoOneZeroDepresin.bank();
    }

    @Override
    public String officialName() {
      return "Neuro-QoL Banco v1.0 - Depresión";
    }
  },
  neuroQolPedBankTwoZeroCognitiveFunction {
    @Override
    public ItemBank bank() {
      return NeuroQolPedBankTwoZeroCognitiveFunction.bank();
    }

    @Override
    public String officialName() {
      return "Neuro-QOL Ped Bank v2.0 - Cognitive Function";
    }
  },
  promisShortFormOneOneAnger5a {
    @Override
    public ItemBank bank() {
      return PromisShortFormOneOneAnger5a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.1 - Anger 5a";
    }
  },
  promisBankTwoZeroCogFunctionAbilitiesSubset {
    @Override
    public ItemBank bank() {
      return PromisBankTwoZeroCogFunctionAbilitiesSubset.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Bank v2.0 - Cog Function Abilities Subset";
    }
  },
  promisParentProxyBankTwoZeroAsthmaImpact {
    @Override
    public ItemBank bank() {
      return PromisParentProxyBankTwoZeroAsthmaImpact.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Parent Proxy Bank v2.0 - Asthma Impact";
    }
  },
  promisShortFormOneZeroSatisfaccinConLaParticipacinEnRolesSociales6a {
    @Override
    public ItemBank bank() {
      return PromisShortFormOneZeroSatisfaccinConLaParticipacinEnRolesSociales6a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0 - Satisfacción con la participación en roles sociales 6a";
    }
  },
  neuroQoLShortFormOneOneSatisfaccinConLaParticipacinEnRolesYActividadesSociales {
    @Override
    public ItemBank bank() {
      return NeuroQoLShortFormOneOneSatisfaccinConLaParticipacinEnRolesYActividadesSociales.bank();
    }

    @Override
    public String officialName() {
      return "Neuro-QoL SF v1.1 - Satisfacción con la participación en roles y actividades sociales";
    }
  },
  promisBankOneZeroSmokingSocialMotiveNonDaily {
    @Override
    public ItemBank bank() {
      return PromisBankOneZeroSmokingSocialMotiveNonDaily.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Bank v1.0 - Smoking Social Motive NonDaily";
    }
  },
  promisPedBankOneZeroPositiveAffect {
    @Override
    public ItemBank bank() {
      return PromisPedBankOneZeroPositiveAffect.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Ped Bank v1.0 - Positive Affect";
    }
  },
  promisPedShortFormOneZeroPsychStressExperiences4a {
    @Override
    public ItemBank bank() {
      return PromisPedShortFormOneZeroPsychStressExperiences4a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Ped SF v1.0 - Psych Stress Experiences 4a";
    }
  },
  promisPedShortFormOneZeroPositiveAffect4a {
    @Override
    public ItemBank bank() {
      return PromisPedShortFormOneZeroPositiveAffect4a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Ped SF v1.0 - Positive Affect 4a";
    }
  },
  promisPedShortFormOneZeroMeaningAndPurpose4a {
    @Override
    public ItemBank bank() {
      return PromisPedShortFormOneZeroMeaningAndPurpose4a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Ped SF v1.0 - Meaning and Purpose 4a";
    }
  },
  promisPedShortFormOneZeroPainBehavior8a {
    @Override
    public ItemBank bank() {
      return PromisPedShortFormOneZeroPainBehavior8a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Ped SF v1.0 - Pain Behavior 8a";
    }
  },
  promisShortFormOneZeroSelfEfficacyManageSymptoms4a {
    @Override
    public ItemBank bank() {
      return PromisShortFormOneZeroSelfEfficacyManageSymptoms4a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0 - Self-Efficacy Manage Symptoms 4a";
    }
  },
  promisParentProxyShortFormOneZeroAnger5a {
    @Override
    public ItemBank bank() {
      return PromisParentProxyShortFormOneZeroAnger5a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Parent Proxy SF v1.0 - Anger 5a";
    }
  },
  promisParentProxyBankTwoZeroPainInterference {
    @Override
    public ItemBank bank() {
      return PromisParentProxyBankTwoZeroPainInterference.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Parent Proxy Bank v2.0 - Pain Interference";
    }
  },
  promisPediatricShortFormOneOneAnxiety8b {
    @Override
    public ItemBank bank() {
      return PromisPediatricShortFormOneOneAnxiety8b.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Pediatric SF v1.1 - Anxiety 8b";
    }
  },
  promisShortFormTwoZeroCognitiveFunction6a {
    @Override
    public ItemBank bank() {
      return PromisShortFormTwoZeroCognitiveFunction6a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v2.0 - Cognitive Function 6a";
    }
  },
  neuroQolBankOneZeroEmotionalBehDyscontrol {
    @Override
    public ItemBank bank() {
      return NeuroQolBankOneZeroEmotionalBehDyscontrol.bank();
    }

    @Override
    public String officialName() {
      return "Neuro-QOL Bank v1.0 - Emotional & Beh. Dyscontrol";
    }
  },
  promisEscalaOneZeroIntensidadDelDolor3a {
    @Override
    public ItemBank bank() {
      return PromisEscalaOneZeroIntensidadDelDolor3a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Escala v1.0 - Intensidad del dolor 3a";
    }
  },
  promisBankOneZeroSmokingEmotSensoryDaily {
    @Override
    public ItemBank bank() {
      return PromisBankOneZeroSmokingEmotSensoryDaily.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Bank v1.0 - Smoking Emot/Sensory Daily";
    }
  },
  promisParentProxyShortFormOneZeroPhysStressExp8a {
    @Override
    public ItemBank bank() {
      return PromisParentProxyShortFormOneZeroPhysStressExp8a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Parent Proxy SF v1.0 - Phys Stress Exp 8a";
    }
  },
  promisShortFormTwoZeroInstrumentalSupport4a {
    @Override
    public ItemBank bank() {
      return PromisShortFormTwoZeroInstrumentalSupport4a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v2.0 - Instrumental Support 4a";
    }
  },
  neuroQoLShortFormPedOneZeroAnsiedad {
    @Override
    public ItemBank bank() {
      return NeuroQoLShortFormPedOneZeroAnsiedad.bank();
    }

    @Override
    public String officialName() {
      return "Neuro-QoL SF Ped  v1.0 - Ansiedad";
    }
  },
  neuroQoLShortFormOneZeroEstigma {
    @Override
    public ItemBank bank() {
      return NeuroQoLShortFormOneZeroEstigma.bank();
    }

    @Override
    public String officialName() {
      return "Neuro-QoL SF v1.0 - Estigma";
    }
  },
  promisPedShortFormOneZeroFatigue10a {
    @Override
    public ItemBank bank() {
      return PromisPedShortFormOneZeroFatigue10a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Ped SF v1.0 - Fatigue 10a";
    }
  },
  promisShortFormOneZeroSmokingNicotineDependDay4a {
    @Override
    public ItemBank bank() {
      return PromisShortFormOneZeroSmokingNicotineDependDay4a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0 - Smoking NicotineDepend Day 4a";
    }
  },
  promisScaleOneZeroPainIntensity3a {
    @Override
    public ItemBank bank() {
      return PromisScaleOneZeroPainIntensity3a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Scale v1.0 - Pain Intensity 3a";
    }
  },
  promisShortFormV10Fatigue6a {
    @Override
    public ItemBank bank() {
      return PromisShortFormV10Fatigue6a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0-Fatigue 6a";
    }
  },
  promisBankOneZeroSmokingHealthExpectDaily {
    @Override
    public ItemBank bank() {
      return PromisBankOneZeroSmokingHealthExpectDaily.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Bank v1.0 - Smoking Health Expect Daily";
    }
  },
  promisShortFormV10PainInterference8a {
    @Override
    public ItemBank bank() {
      return PromisShortFormV10PainInterference8a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0-Pain Interference 8a";
    }
  },
  promisPedShortFormOneZeroStrengthImpact4a {
    @Override
    public ItemBank bank() {
      return PromisPedShortFormOneZeroStrengthImpact4a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Ped SF v1.0 - Strength Impact 4a";
    }
  },
  promisPedShortFormOneZeroPeerRel8a {
    @Override
    public ItemBank bank() {
      return PromisPedShortFormOneZeroPeerRel8a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Ped SF v1.0 - Peer Rel 8a";
    }
  },
  promisShortFormOneZeroDepresin8a {
    @Override
    public ItemBank bank() {
      return PromisShortFormOneZeroDepresin8a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0 - Depresión 8a";
    }
  },
  neuroQoLBancoOneOneSatisfaccinConLaParticipacinEnRolesYActividadesSociales {
    @Override
    public ItemBank bank() {
      return NeuroQoLBancoOneOneSatisfaccinConLaParticipacinEnRolesYActividadesSociales.bank();
    }

    @Override
    public String officialName() {
      return "Neuro-QoL Banco v1.1 - Satisfacción con la participación en roles y actividades sociales";
    }
  },
  promisShortFormOneZeroDepresin8b {
    @Override
    public ItemBank bank() {
      return PromisShortFormOneZeroDepresin8b.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0 - Depresión 8b";
    }
  },
  promisShortFormTwoZeroSocialIsolation4a {
    @Override
    public ItemBank bank() {
      return PromisShortFormTwoZeroSocialIsolation4a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v2.0 - Social Isolation 4a";
    }
  },
  promisShortFormOneZeroAppliedCogAbilities8a {
    @Override
    public ItemBank bank() {
      return PromisShortFormOneZeroAppliedCogAbilities8a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0 - Applied Cog Abilities 8a";
    }
  },
  neuroQoLBancoPedV21Agotamiento {
    @Override
    public ItemBank bank() {
      return NeuroQoLBancoPedV21Agotamiento.bank();
    }

    @Override
    public String officialName() {
      return "Neuro-QoL Banco Ped  v2.1- Agotamiento";
    }
  },
  neuroQoLShortFormOneZeroAnsiedad {
    @Override
    public ItemBank bank() {
      return NeuroQoLShortFormOneZeroAnsiedad.bank();
    }

    @Override
    public String officialName() {
      return "Neuro-QoL SF v1.0 - Ansiedad";
    }
  },
  promisPedShortFormOneZeroCognitiveFunction7a {
    @Override
    public ItemBank bank() {
      return PromisPedShortFormOneZeroCognitiveFunction7a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Ped SF v1.0 - Cognitive Function 7a";
    }
  },
  neuroQolPedShortFormV21Fatigue {
    @Override
    public ItemBank bank() {
      return NeuroQolPedShortFormV21Fatigue.bank();
    }

    @Override
    public String officialName() {
      return "Neuro-QOL Ped SF v2.1 - Fatigue";
    }
  },
  promisBankOneZeroSmokingCopingExpectDaily {
    @Override
    public ItemBank bank() {
      return PromisBankOneZeroSmokingCopingExpectDaily.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Bank v1.0 - Smoking Coping Expect Daily";
    }
  },
  promisShortFormOneZeroSmokingPsychSocExpect6aAllSmk {
    @Override
    public ItemBank bank() {
      return PromisShortFormOneZeroSmokingPsychSocExpect6aAllSmk.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0 - Smoking PsychSocExpect 6a AllSmk";
    }
  },
  promisShortFormOneZeroSelfEfficacyManageMedsTx8a {
    @Override
    public ItemBank bank() {
      return PromisShortFormOneZeroSelfEfficacyManageMedsTx8a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0 - Self-Efficacy Manage Meds/Tx 8a";
    }
  },
  neuroQolPedBankOneZeroStigma {
    @Override
    public ItemBank bank() {
      return NeuroQolPedBankOneZeroStigma.bank();
    }

    @Override
    public String officialName() {
      return "Neuro-QOL Ped Bank v1.0 - Stigma";
    }
  },
  neuroQoLBancoPedOneZeroAnsiedad {
    @Override
    public ItemBank bank() {
      return NeuroQoLBancoPedOneZeroAnsiedad.bank();
    }

    @Override
    public String officialName() {
      return "Neuro-QoL Banco Ped  v1.0 - Ansiedad";
    }
  },
  promisShortFormOneZeroSmokingCopingExpectDay4a {
    @Override
    public ItemBank bank() {
      return PromisShortFormOneZeroSmokingCopingExpectDay4a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0 - Smoking Coping Expect Day 4a";
    }
  },
  promisShortFormTwoZeroCognitiveFunction8a {
    @Override
    public ItemBank bank() {
      return PromisShortFormTwoZeroCognitiveFunction8a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v2.0 - Cognitive Function 8a";
    }
  },
  neuroQoLShortFormPedTwoZeroFuncinCognitiva {
    @Override
    public ItemBank bank() {
      return NeuroQoLShortFormPedTwoZeroFuncinCognitiva.bank();
    }

    @Override
    public String officialName() {
      return "Neuro-QoL SF Ped  v2.0 - Función Cognitiva";
    }
  },
  promisShortFormOneZeroDepression8b {
    @Override
    public ItemBank bank() {
      return PromisShortFormOneZeroDepression8b.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0 - Depression 8b";
    }
  },
  promisBankOneZeroSmokingCopingExpectNonDaily {
    @Override
    public ItemBank bank() {
      return PromisBankOneZeroSmokingCopingExpectNonDaily.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Bank v1.0 - Smoking Coping Expect NonDaily";
    }
  },
  promisShortFormTwoZeroInstrumentalSupport6a {
    @Override
    public ItemBank bank() {
      return PromisShortFormTwoZeroInstrumentalSupport6a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v2.0 - Instrumental Support 6a";
    }
  },
  promisShortFormOneZeroSelfEfficacyManageSocInter8a {
    @Override
    public ItemBank bank() {
      return PromisShortFormOneZeroSelfEfficacyManageSocInter8a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0 - Self-Efficacy Manage Soc Inter 8a";
    }
  },
  promisParentProxyBankOneZeroPainBehavior {
    @Override
    public ItemBank bank() {
      return PromisParentProxyBankOneZeroPainBehavior.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Parent Proxy Bank v1.0 - Pain Behavior";
    }
  },
  promisShortFormTwoZeroCompanionship6a {
    @Override
    public ItemBank bank() {
      return PromisShortFormTwoZeroCompanionship6a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v2.0 - Companionship 6a";
    }
  },
  promisShortFormTwoZeroInformationalSupport8a {
    @Override
    public ItemBank bank() {
      return PromisShortFormTwoZeroInformationalSupport8a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v2.0 - Informational Support 8a";
    }
  },
  promisShortFormV10Fatigue4a {
    @Override
    public ItemBank bank() {
      return PromisShortFormV10Fatigue4a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0-Fatigue 4a";
    }
  },
  promisShortFormOneZeroSmokingEmotSensoryNonday6a {
    @Override
    public ItemBank bank() {
      return PromisShortFormOneZeroSmokingEmotSensoryNonday6a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0 - Smoking Emot/Sensory Nonday 6a";
    }
  },
  neuroQolPedShortFormTwoZeroCognitiveFunction {
    @Override
    public ItemBank bank() {
      return NeuroQolPedShortFormTwoZeroCognitiveFunction.bank();
    }

    @Override
    public String officialName() {
      return "Neuro-QOL Ped SF v2.0 - Cognitive Function";
    }
  },
  neuroQoLShortFormOneZeroTrastornosDelSueo {
    @Override
    public ItemBank bank() {
      return NeuroQoLShortFormOneZeroTrastornosDelSueo.bank();
    }

    @Override
    public String officialName() {
      return "Neuro-QoL SF v1.0 - Trastornos del sueño";
    }
  },
  promisPediatricShortFormOneOneDepressiveSymptoms8b {
    @Override
    public ItemBank bank() {
      return PromisPediatricShortFormOneOneDepressiveSymptoms8b.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Pediatric SF v1.1 - Depressive Symptoms 8b";
    }
  },
  neuroQoLBancoOneZeroAgotamiento {
    @Override
    public ItemBank bank() {
      return NeuroQoLBancoOneZeroAgotamiento.bank();
    }

    @Override
    public String officialName() {
      return "Neuro-QoL Banco v1.0 - Agotamiento";
    }
  },
  neuroQoLShortFormOneZeroAgotamiento {
    @Override
    public ItemBank bank() {
      return NeuroQoLShortFormOneZeroAgotamiento.bank();
    }

    @Override
    public String officialName() {
      return "Neuro-QoL SF v1.0 - Agotamiento";
    }
  },
  promisShortFormOneZeroPainBehavior7a {
    @Override
    public ItemBank bank() {
      return PromisShortFormOneZeroPainBehavior7a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0 - Pain Behavior 7a";
    }
  },
  promisShortFormTwoZeroSocialIsolation6a {
    @Override
    public ItemBank bank() {
      return PromisShortFormTwoZeroSocialIsolation6a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v2.0 - Social Isolation 6a";
    }
  },
  promisShortFormV10PainInterference6a {
    @Override
    public ItemBank bank() {
      return PromisShortFormV10PainInterference6a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0-Pain Interference 6a";
    }
  },
  promisPedShortFormOneZeroPhysicalActivity8a {
    @Override
    public ItemBank bank() {
      return PromisPedShortFormOneZeroPhysicalActivity8a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Ped SF v1.0 - Physical Activity 8a";
    }
  },
  promisShortFormOneZeroDepresin6a {
    @Override
    public ItemBank bank() {
      return PromisShortFormOneZeroDepresin6a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0 - Depresión 6a";
    }
  },
  promisParentProxyShortFormTwoZeroPainInterfere8a {
    @Override
    public ItemBank bank() {
      return PromisParentProxyShortFormTwoZeroPainInterfere8a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Parent Proxy SF v2.0 - Pain Interfere 8a";
    }
  },
  promisShortFormOneZeroSmokingEmotSensory6aAllSmk {
    @Override
    public ItemBank bank() {
      return PromisShortFormOneZeroSmokingEmotSensory6aAllSmk.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0 - Smoking Emot/Sensory 6a AllSmk";
    }
  },
  promisShortFormOneZeroAppliedCogAbilities6a {
    @Override
    public ItemBank bank() {
      return PromisShortFormOneZeroAppliedCogAbilities6a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0 - Applied Cog Abilities 6a";
    }
  },
  promisShortFormOneZeroPhysFunction10a {
    @Override
    public ItemBank bank() {
      return PromisShortFormOneZeroPhysFunction10a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0 - Phys. Function 10a";
    }
  },
  promisShortFormOneZeroSmokingNicotineDepend4aAllSmk {
    @Override
    public ItemBank bank() {
      return PromisShortFormOneZeroSmokingNicotineDepend4aAllSmk.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0 - Smoking NicotineDepend 4a AllSmk";
    }
  },
  promisParentProxyShortFormV11DepressiveSymptoms6b {
    @Override
    public ItemBank bank() {
      return PromisParentProxyShortFormV11DepressiveSymptoms6b.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Parent Proxy SF v1.1-Depressive Symptoms 6b";
    }
  },
  promisShortFormOneZeroSmokingPsychSocExpectNonday6a {
    @Override
    public ItemBank bank() {
      return PromisShortFormOneZeroSmokingPsychSocExpectNonday6a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0 - Smoking PsychSocExpect Nonday 6a";
    }
  },
  neuroQolPedShortFormOneZeroPain {
    @Override
    public ItemBank bank() {
      return NeuroQolPedShortFormOneZeroPain.bank();
    }

    @Override
    public String officialName() {
      return "Neuro-QOL Ped SF v1.0 - Pain";
    }
  },
  promisPedShortFormOneZeroLifeSatisfaction4a {
    @Override
    public ItemBank bank() {
      return PromisPedShortFormOneZeroLifeSatisfaction4a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Ped SF v1.0 - Life Satisfaction 4a";
    }
  },
  promisParentProxyBankOneZeroPhysicalActivity {
    @Override
    public ItemBank bank() {
      return PromisParentProxyBankOneZeroPhysicalActivity.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Parent Proxy Bank v1.0 - Physical Activity";
    }
  },
  promisPedShortFormOneZeroPhysicalStressExperience4a {
    @Override
    public ItemBank bank() {
      return PromisPedShortFormOneZeroPhysicalStressExperience4a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Ped SF v1.0 - Physical Stress Experience 4a";
    }
  },
  promisShortFormOneZeroSelfEfficacyManageSymptoms8a {
    @Override
    public ItemBank bank() {
      return PromisShortFormOneZeroSelfEfficacyManageSymptoms8a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0 - Self-Efficacy Manage Symptoms 8a";
    }
  },
  promisShortFormOneZeroFatigue7a {
    @Override
    public ItemBank bank() {
      return PromisShortFormOneZeroFatigue7a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0 - Fatigue 7a";
    }
  },
  promisCaBankOneZeroAnxiety {
    @Override
    public ItemBank bank() {
      return PromisCaBankOneZeroAnxiety.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS-Ca Bank v1.0 - Anxiety ";
    }
  },
  promisBancoOneZeroSatisfaccinConLaParticipacinEnActividadesSocialesDiscrecionales {
    @Override
    public ItemBank bank() {
      return PromisBancoOneZeroSatisfaccinConLaParticipacinEnActividadesSocialesDiscrecionales.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Banco v1.0 - Satisfacción con la participación en actividades sociales discrecionales";
    }
  },
  promisShortFormOneZeroSmokingHealthExpectNonday6a {
    @Override
    public ItemBank bank() {
      return PromisShortFormOneZeroSmokingHealthExpectNonday6a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0 - Smoking Health Expect Nonday 6a";
    }
  },
  neuroQolShortFormOneZeroPosAffectWellBeing {
    @Override
    public ItemBank bank() {
      return NeuroQolShortFormOneZeroPosAffectWellBeing.bank();
    }

    @Override
    public String officialName() {
      return "Neuro-QOL SF v1.0 Pos. Affect & Well-Being";
    }
  },
  promisPedShortFormOneZeroMeaningAndPurpose8a {
    @Override
    public ItemBank bank() {
      return PromisPedShortFormOneZeroMeaningAndPurpose8a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Ped SF v1.0 - Meaning and Purpose 8a";
    }
  },
  promisShortFormOneZeroSelfEfficacyManageDayActiv4a {
    @Override
    public ItemBank bank() {
      return PromisShortFormOneZeroSelfEfficacyManageDayActiv4a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0 - Self-Efficacy Manage Day Activ 4a";
    }
  },
  promisBancoOneZeroDepresin {
    @Override
    public ItemBank bank() {
      return PromisBancoOneZeroDepresin.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Banco v1.0 - Depresión";
    }
  },
  promisShortFormOneZeroSatisfaccinConLaParticipacinEnActividadesSocialesDiscrecionales7a {
    @Override
    public ItemBank bank() {
      return PromisShortFormOneZeroSatisfaccinConLaParticipacinEnActividadesSocialesDiscrecionales7a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0 - Satisfacción con la participación en actividades sociales discrecionales 7a";
    }
  },
  neuroQoLBancoPedTwoZeroFuncinCognitiva {
    @Override
    public ItemBank bank() {
      return NeuroQoLBancoPedTwoZeroFuncinCognitiva.bank();
    }

    @Override
    public String officialName() {
      return "Neuro-QoL Banco Ped  v2.0 - Función cognitiva";
    }
  },
  promisParentProxyBankOneZeroPsychStressExp {
    @Override
    public ItemBank bank() {
      return PromisParentProxyBankOneZeroPsychStressExp.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Parent Proxy Bank v1.0 - Psych Stress Exp";
    }
  },
  promisShortFormV10SocialSatRole6a {
    @Override
    public ItemBank bank() {
      return PromisShortFormV10SocialSatRole6a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0-Social Sat Role 6a";
    }
  },
  promisShortFormTwoZeroInstrumentalSupport8a {
    @Override
    public ItemBank bank() {
      return PromisShortFormTwoZeroInstrumentalSupport8a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v2.0 - Instrumental Support 8a";
    }
  },
  promisParentProxyBankOneZeroLifeSatisfaction {
    @Override
    public ItemBank bank() {
      return PromisParentProxyBankOneZeroLifeSatisfaction.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Parent Proxy Bank v1.0 - Life Satisfaction";
    }
  },
  promisBankOneZeroInterestInSexualActivity {
    @Override
    public ItemBank bank() {
      return PromisBankOneZeroInterestInSexualActivity.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Bank v1.0 - Interest in Sexual Activity";
    }
  },
  promisPedShortFormOneZeroAsthma8a {
    @Override
    public ItemBank bank() {
      return PromisPedShortFormOneZeroAsthma8a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Ped SF v1.0 - Asthma 8a";
    }
  },
  promisPedShortFormOneZeroStrengthImpact8a {
    @Override
    public ItemBank bank() {
      return PromisPedShortFormOneZeroStrengthImpact8a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Ped SF v1.0 - Strength Impact 8a";
    }
  },
  promisShortFormV10PainInterference4a {
    @Override
    public ItemBank bank() {
      return PromisShortFormV10PainInterference4a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0-Pain Interference 4a";
    }
  },
  promisShortFormTwoZeroSocialIsolation8a {
    @Override
    public ItemBank bank() {
      return PromisShortFormTwoZeroSocialIsolation8a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v2.0 - Social Isolation 8a";
    }
  },
  promisBankOneZeroSmokingPsychSocExpectAllSmk {
    @Override
    public ItemBank bank() {
      return PromisBankOneZeroSmokingPsychSocExpectAllSmk.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Bank v1.0 - Smoking PsychSocExpect AllSmk";
    }
  },
  promisBankOneZeroSmokingNicotineDependAllSmk {
    @Override
    public ItemBank bank() {
      return PromisBankOneZeroSmokingNicotineDependAllSmk.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Bank v1.0 - Smoking NicotineDepend AllSmk";
    }
  },
  promisBankTwoZeroCognitiveFunction {
    @Override
    public ItemBank bank() {
      return PromisBankTwoZeroCognitiveFunction.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Bank v2.0 - Cognitive Function";
    }
  },
  promisShortFormTwoZeroEmotionalSupport4a {
    @Override
    public ItemBank bank() {
      return PromisShortFormTwoZeroEmotionalSupport4a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v2.0 - Emotional Support 4a";
    }
  },
  promisShortFormOneZeroAppliedCogAbilities4a {
    @Override
    public ItemBank bank() {
      return PromisShortFormOneZeroAppliedCogAbilities4a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0 - Applied Cog Abilities 4a";
    }
  },
  promisBankOneZeroSmokingPsychSocExpectDaily {
    @Override
    public ItemBank bank() {
      return PromisBankOneZeroSmokingPsychSocExpectDaily.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Bank v1.0 - Smoking PsychSocExpect Daily";
    }
  },
  promisParentProxyScaleOneZeroGlobalHealth72 {
    @Override
    public ItemBank bank() {
      return PromisParentProxyScaleOneZeroGlobalHealth72.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Parent Proxy Scale v1.0 - Global Health 7+2";
    }
  },
  promisPediatricScaleOneOneAnger5a {
    @Override
    public ItemBank bank() {
      return PromisPediatricScaleOneOneAnger5a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Pediatric Scale v1.1 - Anger 5a";
    }
  },
  neuroQolPedShortFormOneZeroAnxiety {
    @Override
    public ItemBank bank() {
      return NeuroQolPedShortFormOneZeroAnxiety.bank();
    }

    @Override
    public String officialName() {
      return "Neuro-QOL Ped SF v1.0 - Anxiety";
    }
  },
  promisShortFormOneZeroSmokingNicotineDependNonday8a {
    @Override
    public ItemBank bank() {
      return PromisShortFormOneZeroSmokingNicotineDependNonday8a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0 - Smoking NicotineDepend Nonday 8a";
    }
  },
  neuroQolBankOneZeroFatigue {
    @Override
    public ItemBank bank() {
      return NeuroQolBankOneZeroFatigue.bank();
    }

    @Override
    public String officialName() {
      return "Neuro-QOL Bank v1.0 - Fatigue";
    }
  },
  promisCaBankOneOnePainInterference {
    @Override
    public ItemBank bank() {
      return PromisCaBankOneOnePainInterference.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS-Ca Bank v1.1 - Pain Interference";
    }
  },
  promisShortFormOneZeroAnsiedad4a {
    @Override
    public ItemBank bank() {
      return PromisShortFormOneZeroAnsiedad4a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0 - Ansiedad 4a";
    }
  },
  promisPedBankOneZeroPhysicalStressExperiences {
    @Override
    public ItemBank bank() {
      return PromisPedBankOneZeroPhysicalStressExperiences.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Ped Bank v1.0 - Physical Stress Experiences";
    }
  },
  promisShortFormOneZeroSmokingSocialMotiveDay4a {
    @Override
    public ItemBank bank() {
      return PromisShortFormOneZeroSmokingSocialMotiveDay4a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0 - Smoking Social Motive Day 4a";
    }
  },
  promisShortFormOneZeroSelfEfficacyManageMedsTx4a {
    @Override
    public ItemBank bank() {
      return PromisShortFormOneZeroSelfEfficacyManageMedsTx4a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0 - Self-Efficacy Manage Meds/Tx 4a";
    }
  },
  promisParentProxyShortFormOneOneAnxiety8b {
    @Override
    public ItemBank bank() {
      return PromisParentProxyShortFormOneOneAnxiety8b.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Parent Proxy SF v1.1 - Anxiety 8b";
    }
  },
  neuroQoLBancoOneZeroDescontrolEmocionalYConductual {
    @Override
    public ItemBank bank() {
      return NeuroQoLBancoOneZeroDescontrolEmocionalYConductual.bank();
    }

    @Override
    public String officialName() {
      return "Neuro-QoL Banco v1.0 - Descontrol emocional y conductual";
    }
  },
  promisParentProxyShortFormOneZeroPositiveAffect8a {
    @Override
    public ItemBank bank() {
      return PromisParentProxyShortFormOneZeroPositiveAffect8a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Parent Proxy SF v1.0 - Positive Affect 8a";
    }
  },
  promisShortFormTwoZeroCognitiveFunction4a {
    @Override
    public ItemBank bank() {
      return PromisShortFormTwoZeroCognitiveFunction4a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v2.0 - Cognitive Function 4a";
    }
  },
  promisParentProxyShortFormTwoZeroAnxiety8a {
    @Override
    public ItemBank bank() {
      return PromisParentProxyShortFormTwoZeroAnxiety8a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Parent Proxy SF v2.0 - Anxiety 8a";
    }
  },
  neuroQolPedBankV21Fatigue {
    @Override
    public ItemBank bank() {
      return NeuroQolPedBankV21Fatigue.bank();
    }

    @Override
    public String officialName() {
      return "Neuro-QOL Ped Bank v2.1- Fatigue";
    }
  },
  neuroQoLShortFormOneZeroDepresin {
    @Override
    public ItemBank bank() {
      return NeuroQoLShortFormOneZeroDepresin.bank();
    }

    @Override
    public String officialName() {
      return "Neuro-QoL SF v1.0 - Depresión";
    }
  },
  neuroQolPedShortFormOneZeroDepression {
    @Override
    public ItemBank bank() {
      return NeuroQolPedShortFormOneZeroDepression.bank();
    }

    @Override
    public String officialName() {
      return "Neuro-QOL Ped SF v1.0 - Depression";
    }
  },
  promisBankOneZeroSmokingSocialMotiveDaily {
    @Override
    public ItemBank bank() {
      return PromisBankOneZeroSmokingSocialMotiveDaily.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Bank v1.0 - Smoking Social Motive Daily";
    }
  },
  promisParentProxyShortFormTwoZeroFatigue10a {
    @Override
    public ItemBank bank() {
      return PromisParentProxyShortFormTwoZeroFatigue10a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Parent Proxy SF v2.0 - Fatigue 10a";
    }
  },
  promisShortFormOneZeroSelfEfficacyManageSocInter4a {
    @Override
    public ItemBank bank() {
      return PromisShortFormOneZeroSelfEfficacyManageSocInter4a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0 - Self-Efficacy Manage Soc Inter 4a";
    }
  },
  promisBancoOneOneEfectosDelDolor {
    @Override
    public ItemBank bank() {
      return PromisBancoOneOneEfectosDelDolor.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Banco v1.1 - Efectos del dolor ";
    }
  },
  promisPediatricScaleOneZeroGlobalHealth72 {
    @Override
    public ItemBank bank() {
      return PromisPediatricScaleOneZeroGlobalHealth72.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Pediatric Scale v1.0 - Global Health 7+2";
    }
  },
  promisBankOneZeroSmokingNicotineDependNonDaily {
    @Override
    public ItemBank bank() {
      return PromisBankOneZeroSmokingNicotineDependNonDaily.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Bank v1.0 - Smoking NicotineDepend NonDaily";
    }
  },
  promisShortFormV10SocialSatRole8a {
    @Override
    public ItemBank bank() {
      return PromisShortFormV10SocialSatRole8a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0-Social Sat Role 8a";
    }
  },
  promisPedShortFormOneZeroPhysicalActivity4a {
    @Override
    public ItemBank bank() {
      return PromisPedShortFormOneZeroPhysicalActivity4a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Ped SF v1.0 - Physical Activity 4a";
    }
  },
  neuroQolBankOneZeroAnxiety {
    @Override
    public ItemBank bank() {
      return NeuroQolBankOneZeroAnxiety.bank();
    }

    @Override
    public String officialName() {
      return "Neuro-QOL Bank v1.0 - Anxiety";
    }
  },
  promisShortFormTwoZeroEmotionalSupport6a {
    @Override
    public ItemBank bank() {
      return PromisShortFormTwoZeroEmotionalSupport6a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v2.0 - Emotional Support 6a";
    }
  },
  promisShortFormOneZeroSmokingHealthExpect6aAllSmk {
    @Override
    public ItemBank bank() {
      return PromisShortFormOneZeroSmokingHealthExpect6aAllSmk.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0 - Smoking Health Expect 6a AllSmk";
    }
  },
  neuroQolPedShortFormOneZeroAnger {
    @Override
    public ItemBank bank() {
      return NeuroQolPedShortFormOneZeroAnger.bank();
    }

    @Override
    public String officialName() {
      return "Neuro-QOL Ped SF v1.0 - Anger";
    }
  },
  promisShortFormOneZeroSmokingHealthExpectDay6a {
    @Override
    public ItemBank bank() {
      return PromisShortFormOneZeroSmokingHealthExpectDay6a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0 - Smoking Health Expect Day 6a";
    }
  },
  promisParentProxyShortFormOneZeroCogFunction7a {
    @Override
    public ItemBank bank() {
      return PromisParentProxyShortFormOneZeroCogFunction7a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Parent Proxy SF v1.0 - Cog Function 7a";
    }
  },
  promisShortFormOneZeroSmokingCopingExpect4aAllSmk {
    @Override
    public ItemBank bank() {
      return PromisShortFormOneZeroSmokingCopingExpect4aAllSmk.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS SF v1.0 - Smoking Coping Expect 4a AllSmk";
    }
  },
  promisPedShortFormOneZeroLifeSatisfaction8a {
    @Override
    public ItemBank bank() {
      return PromisPedShortFormOneZeroLifeSatisfaction8a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Ped SF v1.0 - Life Satisfaction 8a";
    }
  },
  promisScaleOneZeroGIBowelIncontinence4a {
    @Override
    public ItemBank bank() {
      return PromisScaleOneZeroGIBowelIncontinence4a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Scale v1.0 - GI Bowel Incontinence 4a";
    }
  },

  promisScaleOneZeroGIDisruptedSwallowing7a {
    @Override
    public ItemBank bank() {
      return PromisScaleOneZeroGIDisruptedSwallowing7a.bank();
    }

    @Override
    public String officialName() {
      return "PROMIS Scale v1.0 - GI Disrupted Swallowing 7a";
    }
  };


  public abstract ItemBank bank();

  public abstract String officialName();

  private static final Map<String, Bank> officialNameToBank = new HashMap<>();
  private static final Map<String, String> synonyms = new HashMap<>();

  static {
    for (Bank bank : Bank.values()) {
      officialNameToBank.put(bank.officialName(), bank);
    }
    synonyms.put("PROMIS Pain Intensity Bank", "PROMIS Bank v1.0 - Pain Interference");
    synonyms.put("PROMIS Pain Behavior Bank", "PROMIS Bank v1.0 - Pain Behavior");
    synonyms.put("PROMIS Physical Function Bank", "PROMIS Bank v1.2 - Physical Function");
    synonyms.put("PROMIS Physical Function - Upper Extremity", "PROMIS Bank v1.2 - Upper Extremity");
    synonyms.put("PROMIS Physical Function - Mobility", "PROMIS Bank v1.2 - Mobility");
    synonyms.put("PROMIS Fatigue Bank", "PROMIS Bank v1.0 - Fatigue");
    synonyms.put("PROMIS Depression Bank", "PROMIS Bank v1.0 - Depression");
    synonyms.put("PROMIS Anxiety Bank", "PROMIS Bank v1.0 - Anxiety");
    synonyms.put("PROMIS Anger Bank", "PROMIS Bank v1.0 - Anger");
    synonyms.put("PROMIS Satisfaction with Social Roles Bank", "PROMIS Bank v2.0 - Satisfaction Roles Activities");
    synonyms.put("PROMIS GI Bowel Incontinence Bank", "PROMIS Scale v1.0 - GI Bowel Incontinence 4a");
    synonyms.put("PROMIS GI Disrupted Swallowing Bank", "PROMIS Scale v1.0 - GI Disrupted Swallowing 7a");
  }

  public static Bank byOfficialName(String officialName) {
    if (synonyms.containsKey(officialName)) {
      officialName = synonyms.get(officialName);
    }
    return officialNameToBank.get(officialName);
  }
}
