commit 0792d37385e60aa8d73f8df174d0a32f4f618bc4
Author: Jungshik Jang <jayjang@google.com>
Date:   Wed Apr 23 17:57:26 2014 +0900

    Implement skeleton of new HDMI Control Service.

    HdmiCecService is a system service handling HDMI-CEC features
    and command. Recently we found out that industry has more
    requirements to support HDMI-CEC. Also, MHL is another
    standard should be in our pocket. Basically, MHL is
    a standard to support communication between mobile device
    and TV or Av device. As CEC is a control standard over HDMI
    cable, MHL has control channel for peer device.
    There behavior is very similiar. Both have commands that
    can change Tv's current input and can send/receive key
    to other device to control other deivce or TV.

    In order to cover both CEC and MHL, current HdmiCecService
    implementation has limitation. We had several
    session of discussion and decided to refactor
    HdmiCecService into HdmiControlService.
    For each standard it will have separate controller instance
    like HdmiCecController and HdmiMhlController.

    In this change I didn't touch original HdmiCecService
    because some component, like cast receiver, uses HdmiCecService.
    For a while we will keep HdmiCecService until HdmiControlService
    accomodates all features of HdmiCecService.

    Change-Id: I5485280ab803dbf071d898bfbe34be0b11ce7958