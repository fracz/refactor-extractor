commit 2cb3ac46093da842c05e709aa7116cff4757e933
Author: Jelmer Schreuder <j.schreuder@mijnpraktijk.com>
Date:   Thu Mar 10 00:25:10 2011 +0100

    Some improvements: models with single primary keys get only that as ID; when using find while expecting only 1 return object (by id, first, last) the object is no longer returned within an array.