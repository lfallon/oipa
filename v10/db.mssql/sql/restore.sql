SET NOCOUNT ON;

-- Parameters
-- :setvar ACTION 'execute'
DECLARE @Action VARCHAR(1000) = N'$(ACTION)'
-- DECLARE @Action VARCHAR(1000) = 'names'
DECLARE @BackupPath VARCHAR(1000) = N'$(BACKUP_PATH)'
-- DECLARE @BackupPath VARCHAR(1000) = '/scratch'
DECLARE @DataPath VARCHAR(1000) = '/var/opt/mssql/data'
-- File Or Directory
DECLARE @FileExists TABLE( FileExists INT, IsDirectory INT, ParentDirExists INT )
INSERT INTO @FileExists
  EXEC master..xp_fileexist @BackupPath
DECLARE @IsFile INT
SELECT @IsFile = FileExists FROM @FileExists


-- List Of Files
DECLARE @FileList TABLE( BackupFile VARCHAR(1000), DEPTH INT, FILEFLAG bit )
DECLARE @Cmd VARCHAR(1000) = 'DIR /b ' + @BackupPath
INSERT INTO @FileList( BackupFile, Depth, FileFlag )
  EXEC master..xp_dirtree @BackupPath, 10, 1
UPDATE @FileList SET BackupFile = @BackupPath + '/' + BackupFile
IF @IsFile = 1
BEGIN
  INSERT INTO @FileList VALUES( @BackupPath, 1, 1 )
END

-- Loop Through Backup Files
DECLARE @BackupFileList TABLE( LogicalName VARCHAR(64), PhysicalName VARCHAR(130), [Type] VARCHAR(1),
 FileGroupName VARCHAR(64), Size DECIMAL(20, 0), MaxSize DECIMAL(25,0), FileID bigint, CreateLSN DECIMAL(25,0),
 DropLSN DECIMAL(25,0), UniqueID UNIQUEIDENTIFIER, ReadOnlyLSN DECIMAL(25,0), ReadWriteLSN DECIMAL(25,0),
 BackupSizeInBytes DECIMAL(25,0), SourceBlockSize INT, filegroupid INT, loggroupguid UNIQUEIDENTIFIER, differentialbaseLSN DECIMAL(25,0),
 differentialbaseGUID UNIQUEIDENTIFIER, isreadonly BIT, ispresent BIT, TDEThumbprint VARBINARY(32), SnapshotURL NVARCHAR(360) )
DECLARE @BackupHeader TABLE( [BackupName] [nvarchar](128), [BackupDescription] [nvarchar](255), [BackupType] [smallint],
      [ExpirationDate] [datetime], [Compressed] [bit], [Position] [smallint], [DeviceType] [tinyint], [UserName] [nvarchar](128),
      [ServerName] [nvarchar](128), [DatabaseName] [nvarchar](128), [DatabaseVersion] [int], [DatabaseCreationDate] [datetime],
      [BackupSize] [numeric](20, 0), [FirstLSN] [numeric](25, 0), [LastLSN] [numeric](25, 0), [CheckpointLSN] [numeric](25, 0),
      [DatabaseBackupLSN] [numeric](25, 0), [BackupStartDate] [datetime], [BackupFinishDate] [datetime], [SortOrder] [smallint],
      [CodePage] [smallint], [UnicodeLocaleId] [int], [UnicodeComparisonStyle] [int], [CompatibilityLevel] [tinyint],
      [SoftwareVendorId] [int], [SoftwareVersionMajor] [int], [SoftwareVersionMinor] [int], [SoftwareVersionBuild] [int],
      [MachineName] [nvarchar](128), [Flags] [int], [BindingID] [uniqueidentifier], [RecoveryForkID] [uniqueidentifier],
      [Collation] [nvarchar](128), [FamilyGUID] [uniqueidentifier], [HasBulkLoggedData] [bit], [IsSnapshot] [bit],
      [IsReadOnly] [bit], [IsSingleUser] [bit], [HasBackupChecksums] [bit], [IsDamaged] [bit], [BeginsLogChain] [bit],
      [HasIncompleteMetaData] [bit], [IsForceOffline] [bit], [IsCOpyOnly] [bit], [FirstRecoveryForkID] [uniqueidentifier],
      [ForkPointLSN] [numeric](25, 0), [RecoveryModel] [nvarchar](60), [DifferentialBaseLSN] [numeric](25, 0),
      [DifferentialBaseGUID] [uniqueidentifier], [BackupTypeDescription] [nvarchar](60), [BackupSetGUID] [uniqueidentifier],
      [CompressedBackupSize] [bigint], containment tinyint not NULL, KeyAlgorithm nvarchar(32), EncryptorThumbprint varbinary(20),
      EncryptorType nvarchar(32) )
DECLARE @BackupFileName VARCHAR(1000)
DECLARE @BackupFilePath VARCHAR(1000)
DECLARE @LogicalDataName VARCHAR(1000)
DECLARE @LogicalLogName VARCHAR(1000)
DECLARE @DataFile VARCHAR(1000)
DECLARE @LogFile VARCHAR(1000)
DECLARE @DatabaseName VARCHAR(1000)
DECLARE @RestoreSql NVARCHAR(4000)
DECLARE @RestoreTable TABLE( FilePath VARCHAR(1000), DatabaseName VARCHAR(1000), RestoreSql NVARCHAR(4000) )
DECLARE db_cursor CURSOR FOR SELECT BackupFile FROM @FileList WHERE BackupFile LIKE '%.BAK'
OPEN db_cursor
FETCH NEXT FROM db_cursor INTO @BackupFileName
WHILE @@FETCH_STATUS = 0
BEGIN
  SET @BackupFilePath = @BackupFileName
  DELETE FROM @BackupFileList
  DELETE FROM @BackupHeader
  INSERT INTO @BackupFileList EXEC('RESTORE FILELISTONLY FROM DISK = ''' + @BackupFilePath + '''' )
  INSERT INTO @BackupHeader EXEC('RESTORE HEADERONLY FROM DISK = ''' + @BackupFilePath + '''' )
  SET @DatabaseName = (SELECT TOP 1 DatabaseName FROM @BackupHeader)
  SET @RestoreSql = NULL
  SELECT @RestoreSql = COALESCE( @RestoreSql + ', ', '') + 'MOVE ''' + LogicalName + ''' TO ''' + @DataPath + '/' + @DatabaseName + '.' + LogicalName + '.mdf''' FROM @BackupFileList WHERE Type='D'
  SELECT @RestoreSql = COALESCE( @RestoreSql + ', ', '') + 'MOVE ''' + LogicalName + ''' TO ''' + @DataPath + '/' + @DatabaseName + '.' + LogicalName + '.ldf''' FROM @BackupFileList WHERE Type='L'
  SET @RestoreSql = 'RESTORE DATABASE ' + @DatabaseName + ' FROM DISK = ''' + @BackupFilePath + ''' WITH REPLACE, ' + @RestoreSql
  INSERT INTO @RestoreTable VALUES( @BackupFilePath, @DatabaseName, @RestoreSql )

  FETCH NEXT FROM db_cursor INTO @BackupFileName
END
CLOSE db_cursor
DEALLOCATE db_cursor

IF @Action = 'execute'
BEGIN
  DECLARE db_cursor2 CURSOR FOR SELECT DatabaseName, RestoreSql FROM @RestoreTable
  OPEN db_cursor2
  FETCH NEXT FROM db_cursor2 INTO @DatabaseName, @RestoreSql
  WHILE @@FETCH_STATUS = 0
  BEGIN
     EXEC sp_executesql @RestoreSql
     FETCH NEXT FROM db_cursor2 INTO @DatabaseName, @RestoreSql
  END
  CLOSE db_cursor2
  DEALLOCATE db_cursor2
END

IF @Action = 'names'
  SELECT TRIM(DatabaseName) FROM @RestoreTable
