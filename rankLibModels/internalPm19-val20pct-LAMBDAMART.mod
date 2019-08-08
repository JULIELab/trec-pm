## LambdaMART
## No. of trees = 1000
## No. of leaves = 10
## No. of threshold candidates = 256
## Learning rate = 0.1
## Stop early = 100

<ensemble>
	<tree id="1" weight="0.1">
		<split>
			<feature> 965 </feature>
			<threshold> 0.11652005 </threshold>
			<split pos="left">
				<feature> 192 </feature>
				<threshold> 0.9375 </threshold>
				<split pos="left">
					<feature> 303 </feature>
					<threshold> 0.04424154 </threshold>
					<split pos="left">
						<output> -1.2627369165420532 </output>
					</split>
					<split pos="right">
						<feature> 413 </feature>
						<threshold> 0.037532777 </threshold>
						<split pos="left">
							<feature> 472 </feature>
							<threshold> 0.0 </threshold>
							<split pos="left">
								<feature> 199 </feature>
								<threshold> -1.5165172 </threshold>
								<split pos="left">
									<feature> 619 </feature>
									<threshold> 0.0 </threshold>
									<split pos="left">
										<feature> 839 </feature>
										<threshold> 0.0 </threshold>
										<split pos="left">
											<feature> 83 </feature>
											<threshold> -0.67801183 </threshold>
											<split pos="left">
												<output> -1.937665343284607 </output>
											</split>
											<split pos="right">
												<output> -0.005207705777138472 </output>
											</split>
										</split>
										<split pos="right">
											<output> 2.0 </output>
										</split>
									</split>
									<split pos="right">
										<output> 2.0 </output>
									</split>
								</split>
								<split pos="right">
									<output> 2.0 </output>
								</split>
							</split>
							<split pos="right">
								<output> 2.0 </output>
							</split>
						</split>
						<split pos="right">
							<output> 2.0 </output>
						</split>
					</split>
				</split>
				<split pos="right">
					<output> 1.7503770589828491 </output>
				</split>
			</split>
			<split pos="right">
				<output> 2.0 </output>
			</split>
		</split>
	</tree>
</ensemble>
