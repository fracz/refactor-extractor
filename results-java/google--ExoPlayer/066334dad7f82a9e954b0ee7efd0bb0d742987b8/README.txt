commit 066334dad7f82a9e954b0ee7efd0bb0d742987b8
Author: Oliver Woodman <olly@google.com>
Date:   Thu Feb 12 17:24:23 2015 +0000

    Continue TsExtractor refactor.

    - Remove TsExtractor's knowledge of Sample.
    - Push handling of Sample objects into SampleQueue as much
      as possible. This is a precursor to replacing Sample objects
      with a different type of backing memory. Ideally, the
      individual readers shouldn't know how the sample data is
      stored. This is true after this CL, with the except of the
      TODO in H264Reader.
    - Avoid double-scanning every H264 sample for NAL units, by
      moving the scan for SEI units from SeiReader into H264Reader.

    Issue: #278