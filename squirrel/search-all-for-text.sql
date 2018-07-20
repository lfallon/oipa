BEGIN
	-- Purpose: To search all columns of all tables for a given search string
	DECLARE @SearchStr VARCHAR(255) = '%Direct%'
	DECLARE @Results TABLE( TableName VARCHAR(1000), ColumnName VARCHAR(1000), ColumnValue VARCHAR(8000) )
	DECLARE @TableName VARCHAR(1000)
	DECLARE @ColumnName VARCHAR(255)
	DECLARE @SearchStr2 VARCHAR(255)

	SET NOCOUNT ON
	SET @TableName = ''
	SET @SearchStr2 = QUOTENAME( '' + @SearchStr + '','''' )

	WHILE @TableName IS NOT NULL
	BEGIN
		SET @ColumnName = ''
		SET @TableName =
		(
			SELECT MIN(QUOTENAME(TABLE_SCHEMA) + '.' + QUOTENAME(TABLE_NAME))
			FROM 	INFORMATION_SCHEMA.TABLES
			WHERE 		TABLE_TYPE = 'BASE TABLE'
				AND	QUOTENAME(TABLE_SCHEMA) + '.' + QUOTENAME(TABLE_NAME) > @TableName
				AND	OBJECTPROPERTY(
						OBJECT_ID(
							QUOTENAME(TABLE_SCHEMA) + '.' + QUOTENAME(TABLE_NAME)
							 ), 'IsMSShipped'
						       ) = 0
		)

        IF @TableName LIKE @SearchStr
        BEGIN
            INSERT INTO @Results VALUES( @TableName, 'N/A', '<-- [TABLE NAME]' )
        END

		WHILE (@TableName IS NOT NULL) AND (@ColumnName IS NOT NULL)
		BEGIN
			SET @ColumnName =
			(
				SELECT MIN(QUOTENAME(COLUMN_NAME))
				FROM 	INFORMATION_SCHEMA.COLUMNS
				WHERE 		TABLE_SCHEMA	= PARSENAME(@TableName, 2)
					AND	TABLE_NAME	= PARSENAME(@TableName, 1)
					AND	DATA_TYPE IN ('char', 'varchar', 'nchar', 'nvarchar')
					AND	QUOTENAME(COLUMN_NAME) > @ColumnName
			)

			IF @ColumnName IS NOT NULL
			BEGIN
				INSERT INTO @Results
				EXEC
				(
					'SELECT ''' + @TableName + ''', ''' + @ColumnName + ''', LEFT(' + @ColumnName + ', 3630) '
                    + ' FROM ' + @TableName + ' (NOLOCK) '
                    + ' WHERE ' + @ColumnName + ' LIKE ' + @SearchStr2
				)
                IF @ColumnName LIKE @SearchStr
                BEGIN
                    INSERT INTO @Results VALUES( @TableName, @ColumnName, '<-- [COLUMN NAME]' )
                END
			END
		END
	END

	SELECT TableName, ColumnName, ColumnValue FROM @Results GROUP BY TableName, ColumnName, ColumnValue ORDER BY 1, 2, 3
END
