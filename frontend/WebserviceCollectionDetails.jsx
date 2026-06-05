/* eslint-disable react/prop-types */
/* eslint-disable no-unused-vars */
/* eslint-disable react/react-in-jsx-scope */
import { useContext, useEffect, useState } from "react";
import { AppContext } from "../../Context/Context";
import { externalResources } from "../../Imports/imports_externals";
import DataView from "../DataViewComponents/DataView";
import CustomSwitch from "../DataViewComponents/CustomSwitch";
import FieldItem from "../DataViewComponents/FieldItem";
import InputField from "../DataViewComponents/InputField";
import AlwaysOnSwitch from "../DataViewComponents/AlwaysOnSwitch";

import Webservices from "../../Pages/Webservices/Webservices";
import { GET_REC, OPEN_INLINE, OPEN_PRED, GET_REC_INLINE, GET_REC_PRED, GET_REC_KEY, InlineableTYPES } from "../WebserviceResourcesList/methsMetadataConstants";
const { uuidv4, useLocation, useNavigate } = externalResources;

const WebserviceCollectionDetails = () => {
	const { state: { aggregates, webservices, activeResource, activeWebservice, resourceDetails, projectResources, connectors }, apiMongo: {  upsert } } = useContext(AppContext);
	const isMongo = connectors?.find(con => con._id === activeResource?.connectorId)?.type?.startsWith("Mongo");
	const [ fields, setFields ] = useState([]);
	const [ selectedStreamFields, setSelectedStreamFields ] = useState([]);
	const [ selectedRecordsFields, setSelectedRecordsFields ] = useState([]);
	const [ showOnlyNativeIds, setShowOnlyNativeIds ] = useState(true);
	const [ showOnlyCustomNames, setShowOnlyCustomNames ] = useState(false);
		
	// Dynamic state variables
	const [ openPredMethod, setOpenPredMethod ] = useState("");
	const [ getRecordsMethod, setGetRecordsMethod ] = useState("");
	const [ getRecordsInlineMethod, setGetRecordsInlineMethod ] = useState("");
	const [ getRecordsPredicateMethod, setGetRecordsPredicateMethod ] = useState("");
	const [ openPredMethodPH, setOpenPredMethodPH ] = useState("");
	const [ getRecordsMethodPH, setGetRecordsMethodPH ] = useState("");
	const [ getRecordsInlineMethodPH, setGetRecordsInlineMethodPH ] = useState("");
	const [ getRecordsPredicateMethodPH, setGetRecordsPredicateMethodPH ] = useState("");
	const [ getRecordByKeyMethodPH, setGetRecordByKeyMethodPH ] = useState("");
	const [ streamPrefix, setStreamPrefix ] = useState("");
	const [ prefixPH, setPrefixPH ] = useState("");
	const [ selectedPK, setSelectedPK ] = useState(undefined);
	const [ allowWildStream, setAllowWildStream ] = useState(false);
	const [ allowWildRecords, setAllowWildRecords ] = useState(false);

	
	// URLs and placeholders
	const urlTemplates = {
		openInlineURL: "/<webservice>/<urlPattern>/<prefix>/inline-stream-url",
		openPredURL: "/<webservice>/<urlPattern>/<prefix>/<stream-with-pred>",
		getRecInlineURL: "/<webservice>/<urlPattern>/<prefix>/<records>",
		getRecKeyURL: "/<webservice>/<urlPattern>/<prefix>/{<keyField>}",
		getRecPredURL: "/<webservice>/<urlPattern>/<prefix>/<records-with-pred>",
		getRecURL: "/<webservice>/<urlPattern>/<prefix>/<stream-get>",
		endpointURL: "/<webservice>/<urlPattern>",
		prefixPH: "<connector>/<collection>"
	};
	// Method name templates
	const methodTemplatesSoap = {
		openPredMethod: "streamOpenPred_<connector>_<collection>",
		getRecordsMethod: "streamGet_<connector>_<collection>",
		getRecordsInlineMethod: "recordsGet_<connector>_<collection>",
		getRecordsPredicateMethod: "recordsGetPred_<connector>_<collection>",
		getRecordByKeyMethod: "recordByKey_<connector>_<collection>"
	};
	const methodTemplatesRest = {
		openPredMethod: "stream-with-pred",
		getRecordsMethod: "stream-get",
		getRecordsInlineMethod: "records",
		getRecordsPredicateMethod: "records-with-pred",
		
	};

	// Dynamic state variables for actual URLs
	const [ openInlineURL, setOpenInlineURL ] = useState("");
	const [ openPredURL, setOpenPredURL ] = useState("");
	const [ getRecInlineURL, setGetRecInlineURL ] = useState("");
	const [ getRecKeyURL, setGetRecKeyURL ] = useState("");
	const [ getRecPredURL, setGetRecPredURL ] = useState("");
	const [ getRecURL, setGetRecURL ] = useState("");
	const [ endpointURL, setEndpointURL ] = useState("");
	

	// Function to replace placeholders in URLs with actual values
	const setUrl = (template) => {
		if (!activeWebservice || !activeResource || !template) return "";

		// Replace placeholders with actual values from activeResource
		return template
			.replace("<connector>", activeResource.owner)
			.replace("<collection>", activeResource.nativeIdentifier)
			.replace("<webservice>", activeWebservice.webserviceName)
			.replace("<urlPattern>", activeWebservice.urlPattern || "app")// If urlPattern doesn't exist, use "app"
			.replace("<prefix>", streamPrefix || prefixPH)
			.replace("<stream-with-pred>", openPredMethod || openPredMethodPH)
			.replace("<stream-get>", getRecordsMethod || getRecordsMethodPH )
			.replace("<records>", getRecordsInlineMethod || getRecordsInlineMethodPH)
			.replace("<records-with-pred>", getRecordsPredicateMethod || getRecordsPredicateMethodPH)
			.replace("<keyField>", selectedPK)
			.replace(/\./g, "_");
			

	};
	useEffect(() => {
		
		const connectorRecord = getWsConnector();			
		// Initialize states with values from mongo (if they exist)
		setStreamPrefix(connectorRecord?.streamPathPrefix || "");
		setOpenPredMethod(connectorRecord?.methsMetadata?.[OPEN_PRED]?.name || "");
		setGetRecordsMethod(connectorRecord?.methsMetadata?.[GET_REC]?.name || "");
		setGetRecordsInlineMethod(connectorRecord?.methsMetadata?.[GET_REC_INLINE]?.name || "");
		setGetRecordsPredicateMethod(connectorRecord?.methsMetadata?.[GET_REC_PRED]?.name || "");
		setAllowWildStream(connectorRecord?.methsMetadata?.[OPEN_INLINE]?.allowWild || false);
		setAllowWildRecords(connectorRecord?.methsMetadata?.[GET_REC_INLINE]?.allowWild || false);
	}, [ activeResource ]);

	// Update all URLs dynamically
	useEffect(() => {
		if (activeResource) {
			setOpenInlineURL(setUrl(urlTemplates.openInlineURL));
			setOpenPredURL(setUrl(urlTemplates.openPredURL));
			setGetRecInlineURL(setUrl(urlTemplates.getRecInlineURL));
			setGetRecKeyURL(setUrl(urlTemplates.getRecKeyURL));
			setGetRecPredURL(setUrl(urlTemplates.getRecPredURL));
			setGetRecURL(setUrl(urlTemplates.getRecURL));
			setEndpointURL(setUrl(urlTemplates.endpointURL));
			setPrefixPH(setUrl(urlTemplates.prefixPH));

			// Method names
			if(activeWebservice.mode === "SOAP") {
				setOpenPredMethodPH(setUrl(methodTemplatesSoap.openPredMethod));
				setGetRecordsMethodPH(setUrl(methodTemplatesSoap.getRecordsMethod));
				setGetRecordsInlineMethodPH(setUrl(methodTemplatesSoap.getRecordsInlineMethod));
				setGetRecordsPredicateMethodPH(setUrl(methodTemplatesSoap.getRecordsPredicateMethod));
				setGetRecordByKeyMethodPH(setUrl(methodTemplatesSoap.getRecordByKeyMethod)); 
			} else {
				setOpenPredMethodPH(methodTemplatesRest.openPredMethod);
				setGetRecordsMethodPH(methodTemplatesRest.getRecordsMethod);
				setGetRecordsInlineMethodPH(methodTemplatesRest.getRecordsInlineMethod);
				setGetRecordsPredicateMethodPH(methodTemplatesRest.getRecordsPredicateMethod);
				setGetRecordByKeyMethodPH(methodTemplatesRest.getRecordByKeyMethod);
			}
		}
	}, [ activeResource, activeWebservice, resourceDetails, streamPrefix, openPredMethod, getRecordsMethod, getRecordsInlineMethod, getRecordsPredicateMethod, selectedPK ]);
	
	useEffect( () => {
		if(activeResource){
			
			if (!resourceDetails) return;
			
			// Determine the project resource (prjRec) based on activeResource type
			const prjRec = activeResource.type === "collection"
				? projectResources.find(
					rec =>
						rec.connectorId === activeResource.connectorId &&
                      rec.nativeIdentifier === activeResource.nativeIdentifier
				): aggregates?.find(aggregate =>					
					aggregate.name === activeResource.nativeIdentifier					
				)?.sources?.find(
					rec =>
						rec.connectorName === activeResource.mainSourceConnector &&
						rec.nativeIdentifier === activeResource.mainSourceIdentifier
				);
			
			if (!prjRec) return;
			
			if (resourceDetails && resourceDetails.fields) {
				let relevantFields = [];

				if (activeResource.type === "aggregate") {
					// For aggregates, filter resourceDetails.fields by prjRec.fields
					// prjRec.fields for aggregates is expected to be an object where keys are field names.
					const aggregatePrjRecFields = prjRec.fields ? Object.keys(prjRec.fields) : []; 
					relevantFields = resourceDetails.fields.filter(field => aggregatePrjRecFields.includes(field.name));
				} else { // activeResource.type === "collection"
					if(prjRec.fields) relevantFields=resourceDetails.fields.filter(field => prjRec.fields.includes(field.name));
					else relevantFields = resourceDetails.fields;
				}
                
				const filteredFields = relevantFields.filter(field => InlineableTYPES.includes(field.type));
				setFields(filteredFields);
				
				const initialSelectedFields = filteredFields.map(field => field.nativeIdentifier);
				const connector = getWsConnector();				
				setSelectedFieldsFromConnector(connector, OPEN_INLINE, setSelectedStreamFields, initialSelectedFields);
				setSelectedFieldsFromConnector(connector, GET_REC_INLINE, setSelectedRecordsFields, initialSelectedFields);
				let pkFromConnector = undefined;
				if (isMongo) {
					pkFromConnector = "_id";
				} else if (connector && connector.methsMetadata?.[GET_REC_KEY]?.fields) {
					pkFromConnector = connector.methsMetadata[GET_REC_KEY].fields[0];
				}
				setSelectedPK(pkFromConnector);
			}
		
					
		}

	}, [ activeResource, resourceDetails, activeWebservice, aggregates, projectResources ]);

	const setSelectedFieldsFromConnector = (connector, key, setter, defaultFields) => {
		if (connector && connector.methsMetadata?.[key]?.fields?.length > 0) {
			setter(connector.methsMetadata[key].fields);
								
		}else if (defaultFields) { 
			setter(defaultFields);
		}
	};

	const updateMethodName = async (mthdName, value) => {
		const updatedConnectors = activeWebservice.connectors.map((conn) => {
			if (
				conn.connector === activeResource.owner &&
				conn.remoteName === activeResource.nativeIdentifier
			) {
				if (mthdName === "streamPathPrefix") {
					// Update streamPathPrefix directly
					return {
						...conn,
						[mthdName]: value,
					};
				} else {
					// Update methsMetadata with { name: value }
					return {
						...conn,
						methsMetadata: {
							...conn.methsMetadata, // Preserve existing metadata
							[mthdName]: { name: value }, // Add or overwrite the mthdName key
						},
					};
				}
			}
			return conn;
		});
		const updatedWebservice = {
			...activeWebservice,
			connectors: updatedConnectors
		};	
		await upsert("WEBSERVICE", { _id: activeWebservice._id }, updatedWebservice);
	};
	const handleMethodNameChange = async (mthdName, value, setter) => {
		let isValid = "";
		if(mthdName === "streamPathPrefix"){
		// Validate if the new streamPrefix is unique within the webservice
			isValid =!value || activeWebservice.connectors.every(
				(conn) => conn.streamPathPrefix !== value ); 
		} else {
			const connRec = activeWebservice.connectors.find((conn) =>  conn.connector === activeResource.owner && conn.remoteName === activeResource.nativeIdentifier );
			if (connRec && connRec.methsMetadata) {
				// Check if any object in methMetadata lacks a property with the same value as `value`
				isValid = !Object.values(connRec.methsMetadata).some(
					(metadata) => metadata.name === value
				);
			}
		}
		if (!isValid) return; 
		// update ws before setting the state
		await updateMethodName(mthdName, value);	
		// Set the new state (this triggers the useEffect)
		if (setter) {
			setter(value);
		}
	};
	

	const saveWsMetadata = async(updatedMetadata) => {
		const updatedConnectors = activeWebservice.connectors.map((conn) => {
			if (conn.connector === activeResource.owner && conn.remoteName === activeResource.nativeIdentifier) {
				return {
					...conn,
					methsMetadata: updatedMetadata,
				};
			}
			return conn;
		});
	
		// Update the webservice
		const updatedWebservice = {
			...activeWebservice,
			connectors: updatedConnectors		
		};
	
		// Perform the update
		await upsert("WEBSERVICE", { _id: activeWebservice._id }, updatedWebservice);
	};


	const toggleAllowWildStream = () => {
		const conRec = getWsConnector();
		if(!conRec) return;
		const updatedMetadata = { ...conRec.methsMetadata };
		updatedMetadata[OPEN_INLINE] = { ...updatedMetadata[OPEN_INLINE], allowWild: !allowWildStream };
		saveWsMetadata(updatedMetadata);
		setAllowWildStream(!allowWildStream);
	};

	const toggleAllowWildRecords = () => {
		const conRec = getWsConnector();
		if(!conRec) return;
		const updatedMetadata = { ...conRec.methsMetadata };
		updatedMetadata[GET_REC_INLINE] = { ...updatedMetadata[GET_REC_INLINE], allowWild: !allowWildRecords };
		saveWsMetadata(updatedMetadata);
		setAllowWildRecords(!allowWildRecords);
	};

	const getWsConnector = () => {
		if(activeWebservice && activeWebservice.connectors){
			const connectorRecord = activeWebservice.connectors.find(connector =>
				connector.connector === activeResource.owner &&
			connector.remoteName === activeResource.nativeIdentifier
			);
			if (!connectorRecord || !connectorRecord.methsMetadata) return ;
			return connectorRecord;
		}
		return undefined;
	};

	const isInWebservice = () => {
		if (activeWebservice?.connectors) {
			return activeWebservice.connectors.some(element => 
				element.connector === activeResource.owner && element.remoteName === activeResource.nativeIdentifier
			);
		}
		return false;
	};
	const isMethodInWebservice = (method) => {
		if (!isInWebservice()) {
			return false; // Check if the webservice is active
		}
	
		// Check if the method exists in the metadata for the active resource
		const connectorRecord = activeWebservice.connectors.find(connector =>
			connector.connector === activeResource.owner &&
			connector.remoteName === activeResource.nativeIdentifier
		);
	
		if (!connectorRecord || !connectorRecord.methsMetadata) {
			return false;
		}
	
		// Check if the method name matches the provided constant (e.g., "open_stream_w_inline")
		return connectorRecord.methsMetadata && Object.keys(connectorRecord.methsMetadata).includes(method);
	};
	async function toggleWsStreamMethod(method) {
		// Extract current metadata for the relevant connector/resource
		const connector = activeWebservice.connectors.find(
			(conn) => conn.connector === activeResource.owner && conn.remoteName === activeResource.nativeIdentifier);
	
		if (!connector) {
			
			return;
		}			
		const updatedMetadata = { ...connector.methsMetadata };
	
		if (isMethodInWebservice(method)) {
			delete updatedMetadata[method]; // Remove method if active
			
		} else {
			updatedMetadata[method] = {}; // Add method if not active
			
		}
		const openStreamMethodKeys = [ OPEN_INLINE, OPEN_PRED ];	
	
		// Determine if "get-stream-records" needs to be updated
		const anyOpenStreamActive = openStreamMethodKeys.some((key) => updatedMetadata[key]);
		const isGetStreamRecordsActive = !!updatedMetadata[GET_REC];
	
		if (isGetStreamRecordsActive && !anyOpenStreamActive) {
			delete updatedMetadata[GET_REC];
		} else if (!isGetStreamRecordsActive && anyOpenStreamActive) {
			updatedMetadata[GET_REC] = {};
		}
	
		// Update the connector with the new metadata
		const updatedConnectors = activeWebservice.connectors.map((conn) => {
			if (conn.connector === activeResource.owner && conn.remoteName === activeResource.nativeIdentifier) {
				return {
					...conn,
					methsMetadata: updatedMetadata,
				};
			}
			return conn;
		});
	
		// Update the webservice
		const updatedWebservice = {
			...activeWebservice,
			connectors: updatedConnectors		
		};
	
		// Perform the update
		await upsert("WEBSERVICE", { _id: activeWebservice._id }, updatedWebservice);
	}
	
	
	const removeField = (field) => {
	};
	const toggleStreamField = (field) => {
		let newStreamFields = selectedStreamFields.includes(field.nativeIdentifier) ? 
			selectedStreamFields.filter((fld) => fld !== field.nativeIdentifier)
			:
			[ ...selectedStreamFields, field.nativeIdentifier ];
		if (newStreamFields.length === 0) {
			newStreamFields = fields.map((f) => f.nativeIdentifier);
		}
		console.log("FFFFFFFFFFF", selectedStreamFields, "new", newStreamFields);
		const conRec = getWsConnector();
		console.log("CCCCCCCCCC", conRec);
		if(!conRec) return;
		const updatedMetadata = { ...conRec.methsMetadata };
		if(newStreamFields.length != fields.length && newStreamFields.length > 0){					
			updatedMetadata[OPEN_INLINE].fields = newStreamFields;
		} else {
			updatedMetadata[OPEN_INLINE].fields = [];
		}
		console.log("GGGGGG", updatedMetadata);
		saveWsMetadata(updatedMetadata);
	};
	
	const toggleRecordsField = (field) => {
		let newRecordsFields = selectedRecordsFields.includes(field.nativeIdentifier) ? 
			selectedRecordsFields.filter((fld) => fld !== field.nativeIdentifier)
			:
			[ ...selectedRecordsFields, field.nativeIdentifier ];
		if (newRecordsFields.length === 0) {
			newRecordsFields = fields.map((f) => f.nativeIdentifier);
		}
		const conRec = getWsConnector();
		if(!conRec) return;
		const updatedMetadata = { ...conRec.methsMetadata };
		if(newRecordsFields.length != fields.length && newRecordsFields.length > 0){					
			updatedMetadata[GET_REC_INLINE].fields = newRecordsFields;
		} else {
			updatedMetadata[GET_REC_INLINE].fields = [];
		}
		saveWsMetadata(updatedMetadata);
	};

	const toggleFieldPK = (field) => {
		const conRec = getWsConnector();
		if(!conRec) return;
		const updatedMetadata = { ...conRec.methsMetadata };		
		updatedMetadata[GET_REC_KEY].fields = [ field.nativeIdentifier ];		
		saveWsMetadata(updatedMetadata);
		setSelectedPK(field.nativeIdentifier);
	};

	// Function to check if field is mapped
	const isMappedStream = (field) => selectedStreamFields.includes(field.nativeIdentifier); // Check if selected
	const isMappedRecords = (field) => selectedRecordsFields.includes(field.nativeIdentifier); // Check if selected
	const isMappedPK = (field) => selectedPK === field.nativeIdentifier;
	const isPKAvailable = (fieldIdentifiers) => {		
		return fieldIdentifiers.some(fieldObj => fieldObj.nativeIdentifier === selectedPK);
	};
	const isWrongMethodName = (methodName) => {
		if (!methodName) {
			return false; // Allow empty names
		}
		// Check for invalid URL characters using regex
		const invalidUrlPattern = /[^a-zA-Z0-9_]/; // Allow only alphanumeric characters and underscores
		if (invalidUrlPattern.test(methodName) ) {
			return true;
		}

		// Check for uniqueness among method names in the resource
		const existingMethods = [
			openPredMethod,
			getRecordsMethod,
			getRecordsInlineMethod,
			getRecordsPredicateMethod, openPredMethodPH, getRecordsInlineMethodPH, getRecordsMethodPH, getRecordsPredicateMethodPH
			
		];
		return existingMethods.filter(name => name === methodName).length > 1; // Allow unique names only
	};
	

	
	
	
	return (
		<div key={activeResource.nativeIdentifier} className="width-adjusted">
			{resourceDetails && (
				<>
					<DataView labelText="Native Name">
						<span> {resourceDetails.nativeIdentifier} </span>
					</DataView>
					{isInWebservice() && (
					
						<>
							{activeWebservice.mode ==="SOAP" ? 
								<DataView labelText="Endpoint URL">
									<span> {endpointURL}</span>
								</DataView> : (
									<InputField 
										value={streamPrefix}
										onChange={(e) => handleMethodNameChange("streamPathPrefix", e.target.value, setStreamPrefix)}
										placeholder={prefixPH}
										labelText="URL Prefix"
									/>
								)}

							{/* Open Stream by Query */}
							{activeWebservice.mode ==="REST" &&(
								<><CustomSwitch label="Open Stream by Query" enabled={isMethodInWebservice(OPEN_INLINE)} onClick={() => toggleWsStreamMethod(OPEN_INLINE)}></CustomSwitch>
									{isMethodInWebservice(OPEN_INLINE)&&(
										<>
											<span className="query-parameters-heading">Query Parameters</span>
											{isMongo && (
												<DataView labelText="Allow generic query parameters" className="margin-top-5">
													<input type="checkbox" checked={allowWildStream} onChange={toggleAllowWildStream} />
												</DataView>
											)}
											<div className="field-container">

												<ul className="tags-list">

													{fields && fields.map(field => (
														<FieldItem
															key={field.nativeIdentifier}
															field={field}
															isMapped={isMappedStream}
															toggleField={toggleStreamField}
															removeField={removeField}
															isActive={true}
															isFieldExpanded={false}
															showDeleteButton={false}
															showOnlyNativeIds={showOnlyNativeIds}
															showOnlyCustomNames={showOnlyCustomNames} />
													))}
												</ul>
											</div>
											<DataView labelText="GET URL">
												<span> {openInlineURL} </span>
											</DataView></>
								
									)}
								</>
							)}
							{/* Open Stream with Predicate */}
				
							<CustomSwitch label= "Open Stream by Predicate" enabled={isMethodInWebservice(OPEN_PRED)} onClick={() => toggleWsStreamMethod(OPEN_PRED)} ></CustomSwitch>
							{isMethodInWebservice(OPEN_PRED)&&(
								<>
									<InputField value={openPredMethod}
										onChange={(e) => handleMethodNameChange(OPEN_PRED, e.target.value, setOpenPredMethod)}
										labelText="Method Name" 
										placeholder={openPredMethodPH}
										hasError={isWrongMethodName(openPredMethod)}/>

									{activeWebservice.mode ==="REST" &&(
										<DataView labelText="POST URL">
											<span> {openPredURL} </span>
										</DataView>
									)}
								</>
							)}
							{/* Get Records from Stream */}
							<AlwaysOnSwitch label= "Get Records from Stream" enabled={isMethodInWebservice(GET_REC)} ></AlwaysOnSwitch>
							{isMethodInWebservice(GET_REC)&&(
								<>
									<InputField value={getRecordsMethod}
										onChange={(e) => handleMethodNameChange(GET_REC, e.target.value, setGetRecordsMethod)}
										labelText="Method Name" 
										placeholder={getRecordsMethodPH}
										hasError={isWrongMethodName(getRecordsMethod)}/>

									{activeWebservice.mode ==="REST" &&(
										<DataView labelText="GET URL">
											<span> {getRecURL} </span>
										</DataView>
									)}
								</>
							)}
							{/* Get Records by Query */}
							{activeWebservice.mode ==="REST" &&(
								<><CustomSwitch label="Get Records by Query" enabled={isMethodInWebservice(GET_REC_INLINE)} onClick={() => toggleWsStreamMethod(GET_REC_INLINE)}></CustomSwitch>
									{isMethodInWebservice(GET_REC_INLINE)&&(
										<>
											<InputField value={getRecordsInlineMethod}
												onChange={(e) => handleMethodNameChange(GET_REC_INLINE, e.target.value, setGetRecordsInlineMethod)}
												placeholder={getRecordsInlineMethodPH}
												labelText="Method Name"
												hasError={isWrongMethodName(getRecordsInlineMethod)} />
											<span className="query-parameters-heading">Query Parameters</span>
											{isMongo && (
												<DataView labelText="Allow generic query parameters" className="margin-top-5">
													<input type="checkbox" checked={allowWildRecords} onChange={toggleAllowWildRecords} />
												</DataView>
											)}
											<div className="field-container">
												<ul className="tags-list">

													{fields && fields.map(field => (
														<FieldItem
															key={field.nativeIdentifier}
															field={field}
															isMapped={isMappedRecords}
															toggleField={toggleRecordsField}
															removeField={removeField}
															isActive={true}
															isFieldExpanded={false}
															showDeleteButton={false}
															showOnlyNativeIds={showOnlyNativeIds}
															showOnlyCustomNames={showOnlyCustomNames} />
													))}
												</ul>
											</div>
											<DataView labelText="GET URL">
												<span> {getRecInlineURL} </span>
											</DataView></>	
									)}
								</>				
							)}
							{/* Get records with Predicate */}
							<CustomSwitch label= "Get Records by Predicate" enabled={isMethodInWebservice(GET_REC_PRED)} onClick={() => toggleWsStreamMethod(GET_REC_PRED)} ></CustomSwitch>
							{isMethodInWebservice(GET_REC_PRED)&&(
								<>
									<InputField value={getRecordsPredicateMethod}
										onChange={(e) => handleMethodNameChange(GET_REC_PRED, e.target.value, setGetRecordsPredicateMethod)}
										placeholder={getRecordsPredicateMethodPH}
										labelText="Method Name" 
										hasError={isWrongMethodName(getRecordsPredicateMethod)}/>
									{activeWebservice.mode ==="REST" &&(
										<DataView labelText="POST URL">
											<span> {getRecPredURL} </span>
										</DataView>
									)}
								</>
							)}
			
							{/* Get Record by Key */}
							<CustomSwitch label= "Get Record by Key" enabled={isMethodInWebservice(GET_REC_KEY)} onClick={() => toggleWsStreamMethod(GET_REC_KEY)} ></CustomSwitch>
							{isMethodInWebservice(GET_REC_KEY) &&(
								<>
									<div className="field-container">
										<span className="query-parameters-heading" >Key Field</span>
										<ul className="tags-list">
											{isMongo ? (
												<FieldItem
													key="_id"
													field={{ nativeIdentifier: "_id", name: "_id" }}
													isMapped={() => true}
													toggleField={() => {}}
													removeField={() => {}}
													isActive={true}
													isFieldExpanded={false}
													showDeleteButton={false}
													showOnlyNativeIds={true}
													showOnlyCustomNames={false}
												/>
											) : (
												fields && fields.map(field => (
													<FieldItem
														key={field.nativeIdentifier}
														field={field}
														isMapped={isMappedPK}
														toggleField={toggleFieldPK}
														removeField={removeField}
														isActive={selectedPK == field.nativeIdentifier}
														isFieldExpanded={false}
														showDeleteButton={false}
														showOnlyNativeIds={showOnlyNativeIds}
														showOnlyCustomNames={showOnlyCustomNames}
													/>
												))
											)}
										</ul></div>
									{activeWebservice.mode ==="REST" &&(
										<DataView labelText="GET URL" labelClassName={(!selectedPK || !isPKAvailable(fields)) ? "item-with-error" : ""}>
											{isPKAvailable(fields) ? (
												<span> {getRecKeyURL} </span>
											) : (
												<span> select a primary key </span>
											)}
										
										</DataView>
									)}
								</>
							)}
						</>
					)}

				</>
			)}
		</div>
	);
};

export default WebserviceCollectionDetails;