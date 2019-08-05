## LambdaMART
## No. of trees = 1000
## No. of leaves = 10
## No. of threshold candidates = 256
## Learning rate = 0.1
## Stop early = 100

<ensemble>
	<tree id="1" weight="0.1">
		<split>
			<feature> 191 </feature>
			<threshold> 0.97077423 </threshold>
			<split pos="left">
				<feature> 427 </feature>
				<threshold> 0.931636 </threshold>
				<split pos="left">
					<feature> 241 </feature>
					<threshold> 0.96668035 </threshold>
					<split pos="left">
						<feature> 412 </feature>
						<threshold> 0.5452869 </threshold>
						<split pos="left">
							<feature> 310 </feature>
							<threshold> 0.2913246 </threshold>
							<split pos="left">
								<output> -1.3037527799606323 </output>
							</split>
							<split pos="right">
								<feature> 426 </feature>
								<threshold> 0.0 </threshold>
								<split pos="left">
									<feature> 84 </feature>
									<threshold> 0.50849134 </threshold>
									<split pos="left">
										<feature> 457 </feature>
										<threshold> 0.23897205 </threshold>
										<split pos="left">
											<feature> 53 </feature>
											<threshold> -0.08222467 </threshold>
											<split pos="left">
												<output> 0.9681932926177979 </output>
											</split>
											<split pos="right">
												<output> -1.1952900886535645 </output>
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
	</tree>
	<tree id="2" weight="0.1">
		<split>
			<feature> 191 </feature>
			<threshold> 0.97077423 </threshold>
			<split pos="left">
				<feature> 427 </feature>
				<threshold> 0.931636 </threshold>
				<split pos="left">
					<feature> 241 </feature>
					<threshold> 0.96668035 </threshold>
					<split pos="left">
						<feature> 310 </feature>
						<threshold> 0.2913246 </threshold>
						<split pos="left">
							<output> -1.2052137851715088 </output>
						</split>
						<split pos="right">
							<feature> 389 </feature>
							<threshold> 0.09883924 </threshold>
							<split pos="left">
								<feature> 412 </feature>
								<threshold> 0.5452869 </threshold>
								<split pos="left">
									<feature> 105 </feature>
									<threshold> 0.87679684 </threshold>
									<split pos="left">
										<feature> 53 </feature>
										<threshold> -0.08222467 </threshold>
										<split pos="left">
											<feature> 208 </feature>
											<threshold> 0.49921525 </threshold>
											<split pos="left">
												<output> 1.5179697275161743 </output>
											</split>
											<split pos="right">
												<output> 0.734178900718689 </output>
											</split>
										</split>
										<split pos="right">
											<output> -1.0173683166503906 </output>
										</split>
									</split>
									<split pos="right">
										<output> 1.718653917312622 </output>
									</split>
								</split>
								<split pos="right">
									<output> 1.7192866802215576 </output>
								</split>
							</split>
							<split pos="right">
								<output> 1.722472071647644 </output>
							</split>
						</split>
					</split>
					<split pos="right">
						<output> 1.7188000679016113 </output>
					</split>
				</split>
				<split pos="right">
					<output> 1.736834168434143 </output>
				</split>
			</split>
			<split pos="right">
				<output> 1.719495415687561 </output>
			</split>
		</split>
	</tree>
	<tree id="3" weight="0.1">
		<split>
			<feature> 191 </feature>
			<threshold> 0.97077423 </threshold>
			<split pos="left">
				<feature> 427 </feature>
				<threshold> 0.931636 </threshold>
				<split pos="left">
					<feature> 241 </feature>
					<threshold> 0.96668035 </threshold>
					<split pos="left">
						<feature> 310 </feature>
						<threshold> 0.2913246 </threshold>
						<split pos="left">
							<output> -1.0524158477783203 </output>
						</split>
						<split pos="right">
							<feature> 426 </feature>
							<threshold> 0.0 </threshold>
							<split pos="left">
								<feature> 619 </feature>
								<threshold> 0.0 </threshold>
								<split pos="left">
									<feature> 13 </feature>
									<threshold> 0.6513857 </threshold>
									<split pos="left">
										<feature> 457 </feature>
										<threshold> 0.23897205 </threshold>
										<split pos="left">
											<feature> 53 </feature>
											<threshold> -0.08222467 </threshold>
											<split pos="left">
												<output> 0.8329747319221497 </output>
											</split>
											<split pos="right">
												<output> -1.0810338258743286 </output>
											</split>
										</split>
										<split pos="right">
											<output> 1.6647576093673706 </output>
										</split>
									</split>
									<split pos="right">
										<output> 1.5364606380462646 </output>
									</split>
								</split>
								<split pos="right">
									<output> 1.537737488746643 </output>
								</split>
							</split>
							<split pos="right">
								<output> 1.5441408157348633 </output>
							</split>
						</split>
					</split>
					<split pos="right">
						<output> 1.5367555618286133 </output>
					</split>
				</split>
				<split pos="right">
					<output> 1.5658782720565796 </output>
				</split>
			</split>
			<split pos="right">
				<output> 1.5381582975387573 </output>
			</split>
		</split>
	</tree>
	<tree id="4" weight="0.1">
		<split>
			<feature> 191 </feature>
			<threshold> 0.97077423 </threshold>
			<split pos="left">
				<feature> 427 </feature>
				<threshold> 0.931636 </threshold>
				<split pos="left">
					<feature> 27 </feature>
					<threshold> 0.8420317 </threshold>
					<split pos="left">
						<feature> 310 </feature>
						<threshold> 0.2913246 </threshold>
						<split pos="left">
							<output> -1.0271574258804321 </output>
						</split>
						<split pos="right">
							<feature> 389 </feature>
							<threshold> 0.09883924 </threshold>
							<split pos="left">
								<feature> 412 </feature>
								<threshold> 0.5452869 </threshold>
								<split pos="left">
									<feature> 193 </feature>
									<threshold> 0.6802652 </threshold>
									<split pos="left">
										<output> 1.281768798828125 </output>
									</split>
									<split pos="right">
										<feature> 259 </feature>
										<threshold> -0.9550395 </threshold>
										<split pos="left">
											<output> 1.7914305925369263 </output>
										</split>
										<split pos="right">
											<feature> 32 </feature>
											<threshold> 0.7415081 </threshold>
											<split pos="left">
												<output> -0.885553777217865 </output>
											</split>
											<split pos="right">
												<output> 0.8051284551620483 </output>
											</split>
										</split>
									</split>
								</split>
								<split pos="right">
									<output> 1.4149346351623535 </output>
								</split>
							</split>
							<split pos="right">
								<output> 1.4191160202026367 </output>
							</split>
						</split>
					</split>
					<split pos="right">
						<output> 1.5143756866455078 </output>
					</split>
				</split>
				<split pos="right">
					<output> 1.4494906663894653 </output>
				</split>
			</split>
			<split pos="right">
				<output> 1.4152097702026367 </output>
			</split>
		</split>
	</tree>
	<tree id="5" weight="0.1">
		<split>
			<feature> 191 </feature>
			<threshold> 0.97077423 </threshold>
			<split pos="left">
				<feature> 27 </feature>
				<threshold> 0.8420317 </threshold>
				<split pos="left">
					<feature> 427 </feature>
					<threshold> 0.931636 </threshold>
					<split pos="left">
						<feature> 310 </feature>
						<threshold> 0.2913246 </threshold>
						<split pos="left">
							<output> -0.9949926733970642 </output>
						</split>
						<split pos="right">
							<feature> 426 </feature>
							<threshold> 0.0 </threshold>
							<split pos="left">
								<feature> 667 </feature>
								<threshold> 0.9499123 </threshold>
								<split pos="left">
									<feature> 193 </feature>
									<threshold> 0.6802652 </threshold>
									<split pos="left">
										<output> 1.2890039682388306 </output>
									</split>
									<split pos="right">
										<feature> 259 </feature>
										<threshold> -0.9550395 </threshold>
										<split pos="left">
											<output> 1.65261971950531 </output>
										</split>
										<split pos="right">
											<feature> 32 </feature>
											<threshold> 0.7415081 </threshold>
											<split pos="left">
												<output> -0.9818684458732605 </output>
											</split>
											<split pos="right">
												<output> 0.7704840898513794 </output>
											</split>
										</split>
									</split>
								</split>
								<split pos="right">
									<output> 1.3254246711730957 </output>
								</split>
							</split>
							<split pos="right">
								<output> 1.3353960514068604 </output>
							</split>
						</split>
					</split>
					<split pos="right">
						<output> 1.3652528524398804 </output>
					</split>
				</split>
				<split pos="right">
					<output> 1.4446086883544922 </output>
				</split>
			</split>
			<split pos="right">
				<output> 1.328914999961853 </output>
			</split>
		</split>
	</tree>
	<tree id="6" weight="0.1">
		<split>
			<feature> 191 </feature>
			<threshold> 0.97077423 </threshold>
			<split pos="left">
				<feature> 27 </feature>
				<threshold> 0.8420317 </threshold>
				<split pos="left">
					<feature> 427 </feature>
					<threshold> 0.931636 </threshold>
					<split pos="left">
						<feature> 310 </feature>
						<threshold> 0.2913246 </threshold>
						<split pos="left">
							<output> -0.9136817455291748 </output>
						</split>
						<split pos="right">
							<feature> 389 </feature>
							<threshold> 0.09883924 </threshold>
							<split pos="left">
								<feature> 619 </feature>
								<threshold> 0.0 </threshold>
								<split pos="left">
									<feature> 259 </feature>
									<threshold> -0.9550395 </threshold>
									<split pos="left">
										<output> 1.546217441558838 </output>
									</split>
									<split pos="right">
										<feature> 193 </feature>
										<threshold> 0.6802652 </threshold>
										<split pos="left">
											<output> 1.1896750926971436 </output>
										</split>
										<split pos="right">
											<feature> 32 </feature>
											<threshold> 0.7415081 </threshold>
											<split pos="left">
												<output> -0.8624578714370728 </output>
											</split>
											<split pos="right">
												<output> 0.6823383569717407 </output>
											</split>
										</split>
									</split>
								</split>
								<split pos="right">
									<output> 1.258065938949585 </output>
								</split>
							</split>
							<split pos="right">
								<output> 1.272026777267456 </output>
							</split>
						</split>
					</split>
					<split pos="right">
						<output> 1.3001378774642944 </output>
					</split>
				</split>
				<split pos="right">
					<output> 1.3547269105911255 </output>
				</split>
			</split>
			<split pos="right">
				<output> 1.2644120454788208 </output>
			</split>
		</split>
	</tree>
	<tree id="7" weight="0.1">
		<split>
			<feature> 191 </feature>
			<threshold> 0.97077423 </threshold>
			<split pos="left">
				<feature> 27 </feature>
				<threshold> 0.8420317 </threshold>
				<split pos="left">
					<feature> 427 </feature>
					<threshold> 0.931636 </threshold>
					<split pos="left">
						<feature> 310 </feature>
						<threshold> 0.2913246 </threshold>
						<split pos="left">
							<output> -0.8448778986930847 </output>
						</split>
						<split pos="right">
							<feature> 389 </feature>
							<threshold> 0.09883924 </threshold>
							<split pos="left">
								<feature> 412 </feature>
								<threshold> 0.5452869 </threshold>
								<split pos="left">
									<feature> 533 </feature>
									<threshold> 0.0 </threshold>
									<split pos="left">
										<feature> 457 </feature>
										<threshold> 0.23897205 </threshold>
										<split pos="left">
											<feature> 159 </feature>
											<threshold> 0.47232997 </threshold>
											<split pos="left">
												<output> 0.9596177339553833 </output>
											</split>
											<split pos="right">
												<output> -0.10688062012195587 </output>
											</split>
										</split>
										<split pos="right">
											<output> 1.3218036890029907 </output>
										</split>
									</split>
									<split pos="right">
										<output> 1.4682468175888062 </output>
									</split>
								</split>
								<split pos="right">
									<output> 1.2077847719192505 </output>
								</split>
							</split>
							<split pos="right">
								<output> 1.2260782718658447 </output>
							</split>
						</split>
					</split>
					<split pos="right">
						<output> 1.252371072769165 </output>
					</split>
				</split>
				<split pos="right">
					<output> 1.28718101978302 </output>
				</split>
			</split>
			<split pos="right">
				<output> 1.2165230512619019 </output>
			</split>
		</split>
	</tree>
	<tree id="8" weight="0.1">
		<split>
			<feature> 261 </feature>
			<threshold> 0.9532662 </threshold>
			<split pos="left">
				<feature> 27 </feature>
				<threshold> 0.8420317 </threshold>
				<split pos="left">
					<feature> 427 </feature>
					<threshold> 0.931636 </threshold>
					<split pos="left">
						<feature> 309 </feature>
						<threshold> 0.03504368 </threshold>
						<split pos="left">
							<output> -1.2845370769500732 </output>
						</split>
						<split pos="right">
							<feature> 132 </feature>
							<threshold> 0.6008019 </threshold>
							<split pos="left">
								<feature> 62 </feature>
								<threshold> -0.7750852 </threshold>
								<split pos="left">
									<output> 1.1682204008102417 </output>
								</split>
								<split pos="right">
									<feature> 518 </feature>
									<threshold> 0.0 </threshold>
									<split pos="left">
										<feature> 308 </feature>
										<threshold> 0.2823016 </threshold>
										<split pos="left">
											<output> -0.5928870439529419 </output>
										</split>
										<split pos="right">
											<feature> 240 </feature>
											<threshold> -1.6070807 </threshold>
											<split pos="left">
												<output> 1.234514594078064 </output>
											</split>
											<split pos="right">
												<output> 0.553778350353241 </output>
											</split>
										</split>
									</split>
									<split pos="right">
										<output> 2.0 </output>
									</split>
								</split>
							</split>
							<split pos="right">
								<output> 1.060617446899414 </output>
							</split>
						</split>
					</split>
					<split pos="right">
						<output> 1.2159628868103027 </output>
					</split>
				</split>
				<split pos="right">
					<output> 1.2353897094726562 </output>
				</split>
			</split>
			<split pos="right">
				<output> 1.5545669794082642 </output>
			</split>
		</split>
	</tree>
	<tree id="9" weight="0.1">
		<split>
			<feature> 309 </feature>
			<threshold> 0.05491299 </threshold>
			<split pos="left">
				<output> -1.0211459398269653 </output>
			</split>
			<split pos="right">
				<feature> 132 </feature>
				<threshold> 0.6008019 </threshold>
				<split pos="left">
					<feature> 427 </feature>
					<threshold> 0.931636 </threshold>
					<split pos="left">
						<feature> 191 </feature>
						<threshold> 0.97077423 </threshold>
						<split pos="left">
							<feature> 518 </feature>
							<threshold> 0.0 </threshold>
							<split pos="left">
								<feature> 167 </feature>
								<threshold> -1.9287951 </threshold>
								<split pos="left">
									<output> 1.2312591075897217 </output>
								</split>
								<split pos="right">
									<feature> 407 </feature>
									<threshold> 0.03521805 </threshold>
									<split pos="left">
										<feature> 313 </feature>
										<threshold> 0.0066256737 </threshold>
										<split pos="left">
											<output> -1.0374090671539307 </output>
										</split>
										<split pos="right">
											<feature> 195 </feature>
											<threshold> 0.54980665 </threshold>
											<split pos="left">
												<output> 1.360276222229004 </output>
											</split>
											<split pos="right">
												<output> 0.233960822224617 </output>
											</split>
										</split>
									</split>
									<split pos="right">
										<output> 1.8743019104003906 </output>
									</split>
								</split>
							</split>
							<split pos="right">
								<output> 1.7215768098831177 </output>
							</split>
						</split>
						<split pos="right">
							<output> 1.147070288658142 </output>
						</split>
					</split>
					<split pos="right">
						<output> 1.188152551651001 </output>
					</split>
				</split>
				<split pos="right">
					<output> 1.1328036785125732 </output>
				</split>
			</split>
		</split>
	</tree>
	<tree id="10" weight="0.1">
		<split>
			<feature> 309 </feature>
			<threshold> 0.05491299 </threshold>
			<split pos="left">
				<output> -1.052861213684082 </output>
			</split>
			<split pos="right">
				<feature> 132 </feature>
				<threshold> 0.6008019 </threshold>
				<split pos="left">
					<feature> 427 </feature>
					<threshold> 0.931636 </threshold>
					<split pos="left">
						<feature> 191 </feature>
						<threshold> 0.97077423 </threshold>
						<split pos="left">
							<feature> 259 </feature>
							<threshold> -1.0557278 </threshold>
							<split pos="left">
								<output> 1.5496577024459839 </output>
							</split>
							<split pos="right">
								<feature> 167 </feature>
								<threshold> -1.9287951 </threshold>
								<split pos="left">
									<output> 1.1548607349395752 </output>
								</split>
								<split pos="right">
									<feature> 313 </feature>
									<threshold> 0.0066256737 </threshold>
									<split pos="left">
										<output> -0.8534358739852905 </output>
									</split>
									<split pos="right">
										<feature> 195 </feature>
										<threshold> 0.5463168 </threshold>
										<split pos="left">
											<output> 1.5475513935089111 </output>
										</split>
										<split pos="right">
											<feature> 253 </feature>
											<threshold> -0.3687269 </threshold>
											<split pos="left">
												<output> 1.311962366104126 </output>
											</split>
											<split pos="right">
												<output> 0.20693756639957428 </output>
											</split>
										</split>
									</split>
								</split>
							</split>
						</split>
						<split pos="right">
							<output> 1.1186418533325195 </output>
						</split>
					</split>
					<split pos="right">
						<output> 1.158313512802124 </output>
					</split>
				</split>
				<split pos="right">
					<output> 1.0903013944625854 </output>
				</split>
			</split>
		</split>
	</tree>
</ensemble>
